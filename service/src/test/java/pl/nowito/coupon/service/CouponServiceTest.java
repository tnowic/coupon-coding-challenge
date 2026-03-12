package pl.nowito.coupon.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static pl.nowito.coupon.error.support.ErrorMessageSupportEnum.*;

class CouponServiceTest {

    @Mock
    private CouponRepository couponRepositoryMock;
    @Mock
    private CustomerRepository customerRepositoryMock;
    @Mock
    private CouponUsageRepository couponUsageRepositoryMock;
    @Mock
    private UserContext userContextMock;

    private CouponService couponService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        couponService = new CouponService(couponRepositoryMock, customerRepositoryMock, couponUsageRepositoryMock, userContextMock);
    }

    @Test
    void testSave() {
        CreateCouponRequest createCouponRequest = new CreateCouponRequest("AAA", 100, "PL", false);
        Coupon newCoupon = CreateCouponRequestConverter.convertToNewEntity(createCouponRequest);
        CouponData expectedCouponData = CouponDataConverter.convertToDto(newCoupon);

        when(couponRepositoryMock.findByCodeIgnoreCase(any())).thenReturn(Optional.empty());
        when(couponRepositoryMock.save(any())).thenReturn(newCoupon);

        CouponData actualCouponData = couponService.save(createCouponRequest);

        assertEquals(expectedCouponData, actualCouponData);
    }

    @Test
    void testSaveThrowsDuplicateCouponCodeException() {
        String duplicateCode = "DUPLICATE";
        CreateCouponRequest createCouponRequestWithDuplicateCode = new CreateCouponRequest(duplicateCode, 100, "PL", false);
        Coupon coupon = buildTestCouponData(duplicateCode);

        when(couponRepositoryMock.findByCodeIgnoreCase(duplicateCode)).thenReturn(Optional.of(coupon));

        assertThrows(DuplicateCouponCodeException.class,
                () -> couponService.save(createCouponRequestWithDuplicateCode));
    }

    @Test
    void testFindByCode() {
        String couponCode = "BBB";
        Coupon coupon = buildTestCouponData(couponCode);
        CouponData expectedCouponData = CouponDataConverter.convertToDto(coupon);

        when(couponRepositoryMock.findByCodeIgnoreCase(couponCode)).thenReturn(Optional.of(coupon));
        CouponData actualCouponData = couponService.findByCode(couponCode);

        assertEquals(expectedCouponData, actualCouponData);
    }

    @Test
    void testFindByCodeNotFound() {
        String missingCode = "MISSINGONE";

        when(couponRepositoryMock.findByCodeIgnoreCase(missingCode)).thenReturn(Optional.empty());

        assertThrows(CouponNotFoundException.class, () -> couponService.findByCode(missingCode));
    }

    @Test
    void testFindAll() {
        List<Coupon> coupons = List.of(buildTestCouponData("AAA"), buildTestCouponData("BBB"));
        Page<Coupon> couponsPage = new PageImpl<>(coupons);
        Page<CouponData> expextedCouponDataPage = couponsPage.map(CouponDataConverter::convertToDto);

        when(couponRepositoryMock.findAll(any(Pageable.class))).thenReturn(couponsPage);

        Page<CouponData> actualResult = couponService.findAll(0, 1);

        assertEquals(expextedCouponDataPage, actualResult);
    }

    @Test
    void testApplyCoupon() {
        String couponCode = "AAA";
        long customerId = 1L;
        ApplyCouponRequest request = new ApplyCouponRequest(customerId);
        Coupon coupon = buildTestCouponData(couponCode);
        int initialCounter = coupon.getCounter();
        Customer customer = buildTestCustomer(customerId);

        when(couponRepositoryMock.findByCodeIgnoreCase(couponCode)).thenReturn(Optional.of(coupon));
        when(userContextMock.getRequestOriginCountryCode()).thenReturn(Optional.of("PL"));
        when(customerRepositoryMock.findById((customerId))).thenReturn(Optional.of(customer));
        when(couponRepositoryMock.save(any())).thenReturn(coupon);

        CouponService couponServiceSpy = spy(couponService);
        CouponData actualCouponData = couponServiceSpy.applyCoupon(couponCode, request);

        verify(couponServiceSpy).verifRequestCountryOrigin(coupon, userContextMock);
        verify(couponServiceSpy).verifyCouponCounter(coupon);
        verify(couponServiceSpy).getCustomer(request, coupon);
        verify(couponServiceSpy).verifyCouponUsage(coupon, customer);

        ArgumentCaptor<CouponUsage> captor = ArgumentCaptor.forClass(CouponUsage.class);
        verify(couponUsageRepositoryMock).save(captor.capture());

        CouponUsage couponUsage = captor.getValue();

        assertNotNull(couponUsage);
        assertNotNull(couponUsage.getCreateTimestamp());
        assertEquals(coupon, couponUsage.getCoupon());
        assertEquals(customer, couponUsage.getCustomer());
        verify(couponUsageRepositoryMock).save(couponUsage);

        assertEquals(initialCounter + 1, coupon.getCounter());
        verify(couponRepositoryMock).save(coupon);

        CouponData expectedCouponData = CouponDataConverter.convertToDto(coupon);
        assertEquals(expectedCouponData, actualCouponData);
    }

    @Test
    void testApplyCouponNotFound() {
        String missingCode = "MISSINGONE";

        when(couponRepositoryMock.findByCodeIgnoreCase(missingCode)).thenReturn(Optional.empty());

        assertThrows(CouponNotFoundException.class,
                () -> couponService.applyCoupon(missingCode, new ApplyCouponRequest(1L)));
    }

    @Test
    void testGetCustomer() {
        long customerId = 1L;
        ApplyCouponRequest request = new ApplyCouponRequest(customerId);
        Coupon couponForRegisteredCustomersOnly = buildTestCouponData("AAA");
        Customer expectedCustomer = new Customer();

        when(customerRepositoryMock.findById((customerId))).thenReturn(Optional.of(expectedCustomer));

        Customer actualCustomer = couponService.getCustomer(request, couponForRegisteredCustomersOnly);

        assertEquals(expectedCustomer, actualCustomer);
    }

    @Test
    void testGetCustomerMissingCustomerInRequest() {
        Coupon coupon = buildTestCouponData("AAA");

        CouponBusinessRuleViolationException exception =
                assertThrows(CouponBusinessRuleViolationException.class,
                        () -> couponService.getCustomer(null, coupon));

        assertEquals(ERROR_COUPON_FOR_REGISTERED_CUSTOMERS_ONLY.getErrorCode(), exception.getErrorCode());
    }

    @Test
    void testGetCustomerRegisteredCustomerNotFound() {
        long customerId = 111L;
        ApplyCouponRequest request = new ApplyCouponRequest(customerId);
        Coupon couponForRegisteredCustomersOnly = buildTestCouponData("AAA");

        when(customerRepositoryMock.findById((customerId))).thenReturn(Optional.empty());

        CouponBusinessRuleViolationException exception =
                assertThrows(CouponBusinessRuleViolationException.class,
                        () -> couponService.getCustomer(request, couponForRegisteredCustomersOnly));

        assertEquals(ERROR_REGISTERED_CUSTOMER_NOT_FOUND.getErrorCode(), exception.getErrorCode());
    }

    @Test
    void testVerifyCouponUsage() {
        Coupon coupon = buildTestCouponData("AAA");
        Customer customer = buildTestCustomer(1L);
        when(couponUsageRepositoryMock.existsByCouponIdAndCustomerId(1L, 1L)).thenReturn(false);

        assertDoesNotThrow(() -> couponService.verifyCouponUsage(coupon, customer));
    }

    @Test
    void testVerifyCouponUsageCuponAlreadyUsedByCustomer() {
        Coupon coupon = buildTestCouponData("AAA");
        Customer testCustomer = buildTestCustomer(1L);

        when(couponUsageRepositoryMock.existsByCouponIdAndCustomerId(1L, 1L)).thenReturn(true);

        CouponBusinessRuleViolationException actualException = assertThrows(CouponBusinessRuleViolationException.class,
                () -> couponService.verifyCouponUsage(coupon, testCustomer));

        assertEquals(ERROR_CUSTOMER_ALREADY_APPLIED_FOR_COUPON.getErrorCode(), actualException.getErrorCode());
    }

    @Test
    void testVerifyCouponCounter() {
        Coupon coupon = buildTestCouponData("AAA");

        couponService.verifyCouponCounter(coupon);

        assertDoesNotThrow(() -> couponService.verifyCouponCounter(coupon));
    }

    @Test
    void testVerifyCouponCounterMaxCounterReached() {
        Coupon coupon = buildTestCouponData("AAA");
        coupon.setCounter(coupon.getMaxCounter());

        CouponBusinessRuleViolationException exception = assertThrows(CouponBusinessRuleViolationException.class,
                () -> couponService.verifyCouponCounter(coupon));

        assertEquals(ERROR_COUPON_MAX_COUNTER_REACHED.getErrorCode(), exception.getErrorCode());
    }

    @Test
    void testVerifRequestCountryOrigin() {
        Coupon coupon = buildTestCouponData("AAA");

        when(userContextMock.getRequestOriginCountryCode()).thenReturn(Optional.of("PL"));

        assertDoesNotThrow(() -> couponService.verifRequestCountryOrigin(coupon, userContextMock));
        verify(userContextMock).getRequestOriginCountryCode();
    }

    @Test
    void testVerifRequestCountryForCouponWithoutRestriction() {
        Coupon coupon = buildTestCouponData("AAA");
        coupon.setCountryCode(null);

        assertDoesNotThrow(() -> couponService.verifRequestCountryOrigin(coupon, userContextMock));
        verify(userContextMock, never()).getRequestOriginCountryCode();
    }

    @Test
    void testVerifRequestCountryOriginMissing() {
        Coupon coupon = buildTestCouponData("AAA");

        when(userContextMock.getRequestOriginCountryCode()).thenReturn(Optional.empty());
        CouponBusinessRuleViolationException exception = assertThrows(CouponBusinessRuleViolationException.class,
                () -> couponService.verifRequestCountryOrigin(coupon, userContextMock));

        assertEquals(ERROR_COUNTRY_CODE_FOR_REQUEST_NOT_FOUND.getErrorCode(), exception.getErrorCode());
    }

    @Test
    void testVerifRequestCountryRestricted() {
        Coupon coupon = buildTestCouponData("AAA");

        when(userContextMock.getRequestOriginCountryCode()).thenReturn(Optional.of("UK"));
        CouponBusinessRuleViolationException exception = assertThrows(CouponBusinessRuleViolationException.class,
                () -> couponService.verifRequestCountryOrigin(coupon, userContextMock));

        assertEquals(ERROR_COUPON_COUNTRY_CODE_RESTRICTED.getErrorCode(), exception.getErrorCode());
    }

    private Coupon buildTestCouponData(String couponCode) {
        Coupon coupon = new Coupon();
        coupon.setCode(couponCode);
        coupon.setMaxCounter(10);
        coupon.setCounter(1);
        coupon.setCountryCode("PL");
        coupon.setForRegUsers(true);
        coupon.setCreateTimestamp(Timestamp.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)));
        Coupon couponSpy = spy(coupon);
        when(couponSpy.getId()).thenReturn(1L);
        return couponSpy;
    }

    private Customer buildTestCustomer(long customerId) {
        Customer customer = mock(Customer.class);
        when(customer.getId()).thenReturn(customerId);
        return customer;
    }
}
