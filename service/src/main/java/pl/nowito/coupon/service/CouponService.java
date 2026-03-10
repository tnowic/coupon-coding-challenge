package pl.nowito.coupon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.nowito.coupon.converter.CouponDataConverter;
import pl.nowito.coupon.converter.CreateCouponRequestConverter;
import pl.nowito.coupon.dto.ApplyCouponRequest;
import pl.nowito.coupon.dto.CouponData;
import pl.nowito.coupon.dto.CreateCouponRequest;
import pl.nowito.coupon.error.CouponBusinessRuleViolationException;
import pl.nowito.coupon.error.CouponNotFoundException;
import pl.nowito.coupon.error.DuplicateCouponCodeException;
import pl.nowito.coupon.model.Coupon;
import pl.nowito.coupon.model.CouponUsage;
import pl.nowito.coupon.model.Customer;
import pl.nowito.coupon.repository.CouponRepository;
import pl.nowito.coupon.repository.CouponUsageRepository;
import pl.nowito.coupon.repository.CustomerRepository;
import pl.nowito.coupon.user.UserContext;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static pl.nowito.coupon.error.support.ErrorMessageSupportEnum.*;

@Service
@Transactional(readOnly = true)
public class CouponService {

    private static final Logger LOG = LoggerFactory.getLogger(CouponService.class);

    private static final String SORT_BY_PROPERTY_CODE = "code";

    private final CouponRepository couponRepository;
    private final CustomerRepository customerRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final UserContext userContext;

    public CouponService(CouponRepository couponRepository,
                         CustomerRepository customerRepository,
                         CouponUsageRepository couponUsageRepository,
                         UserContext userContext) {
        this.couponRepository = couponRepository;
        this.customerRepository = customerRepository;
        this.couponUsageRepository = couponUsageRepository;
        this.userContext = userContext;
    }

    @Transactional
    public CouponData save(CreateCouponRequest createCouponRequest) {
        Optional<Coupon> couponOptional = couponRepository.findByCodeIgnoreCase(createCouponRequest.code());
        if (couponOptional.isPresent()) {
            String couponCodeInUse = couponOptional.get().getCode();
            LOG.warn("Coupon code: {} already in use", couponCodeInUse);
            throw new DuplicateCouponCodeException(couponCodeInUse);
        }
        Coupon newCoupon = CreateCouponRequestConverter.convertToNewEntity(createCouponRequest);
        return CouponDataConverter.convertToDto(couponRepository.save(newCoupon));
    }

    public CouponData findByCode(String code) {
        Optional<Coupon> couponOptional = couponRepository.findByCodeIgnoreCase(code);
        if (couponOptional.isEmpty()) {
            LOG.warn("Coupon code: {} not found", code);
        }
        return couponOptional.map(CouponDataConverter::convertToDto).orElseThrow(() -> new CouponNotFoundException(code));
    }

    public Page<CouponData> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size).withSort(Sort.by(Sort.Direction.ASC, SORT_BY_PROPERTY_CODE));
        Page<Coupon> pageCoupon = couponRepository.findAll(pageable);
        return pageCoupon.map(CouponDataConverter::convertToDto);
    }

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttemptsExpression = "${retry.maxAttempts:3}",
            backoff = @Backoff(delayExpression = "${retry.maxDelay:500}")
    )
    @Transactional
    public CouponData applyCoupon(String code, ApplyCouponRequest applyCouponRequest) {
        Optional<Coupon> couponOptional = couponRepository.findByCodeIgnoreCase(code);
        if (couponOptional.isPresent()) {
            Coupon coupon = couponOptional.get();
            verifRequestCountryOrigin(coupon, userContext);
            verifyCouponCounter(coupon);
            if (Boolean.TRUE.equals(coupon.isForRegUsers())) {
                Customer customer = getCustomer(applyCouponRequest, coupon);
                verifyCouponUsage(coupon, customer);

                CouponUsage couponUsage = new CouponUsage();
                couponUsage.setCoupon(coupon);
                couponUsage.setCustomer(customer);
                couponUsage.setCreateTimestamp(Timestamp.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)));
                couponUsageRepository.save(couponUsage);
            }
            coupon.setCounter(coupon.getCounter() + 1);
            return CouponDataConverter.convertToDto(couponRepository.save(coupon));
        } else {
            LOG.warn("Coupon code: {} not found", code);
            throw new CouponNotFoundException(code);
        }
    }

    Customer getCustomer(ApplyCouponRequest applyCouponRequest, Coupon coupon) {
        if (applyCouponRequest == null) {
            throw new CouponBusinessRuleViolationException(ERROR_COUPON_FOR_REGISTERED_USERS_ONLY, coupon.getCode());
        }
        Optional<Customer> customerOptional = customerRepository.findById(applyCouponRequest.customerId());
        if (customerOptional.isEmpty()) {
            throw new CouponBusinessRuleViolationException(ERROR_REGISTERED_CUSTOMER_NOT_FOUND, applyCouponRequest.customerId());
        }
        return customerOptional.get();
    }

    void verifyCouponUsage(Coupon coupon, Customer customer) {
        if (couponUsageRepository.existsByCouponIdAndCustomerId(coupon.getId(), customer.getId())) {
            throw new CouponBusinessRuleViolationException(ERROR_CUSTOMER_ALREADY_APPLIED_FOR_COUPON, customer.getId(), coupon.getCode());
        }
    }

    void verifyCouponCounter(Coupon coupon) {
        if (coupon.getCounter().equals(coupon.getMaxCounter())) {
            throw new CouponBusinessRuleViolationException(ERROR_COUPON_MAX_COUNTER_REACHED, coupon.getCode(), coupon.getCounter());
        }
    }

    private void verifRequestCountryOrigin(Coupon coupon, UserContext userContext) {
        if (coupon.getCountryCode() != null) {
            String requestOriginCountryCode = userContext.getRequestOriginCountryCode().orElseThrow(() -> new CouponBusinessRuleViolationException(
                    ERROR_COUNTRY_CODE_FOR_REQUEST_NOT_FOUND, coupon.getCode(), coupon.getCountryCode()));
            if (!coupon.getCountryCode().equalsIgnoreCase(requestOriginCountryCode)) {
                throw new CouponBusinessRuleViolationException(ERROR_COUPON_COUNTRY_CODE_RESTRICTED,
                        requestOriginCountryCode, coupon.getCountryCode(), coupon.getCode());
            }
        }
    }
}

