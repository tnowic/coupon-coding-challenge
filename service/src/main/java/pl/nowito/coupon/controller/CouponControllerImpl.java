package pl.nowito.coupon.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.nowito.coupon.dto.ApplyCouponRequest;
import pl.nowito.coupon.dto.CouponData;
import pl.nowito.coupon.dto.CreateCouponRequest;
import pl.nowito.coupon.service.CouponService;

import static java.lang.String.format;

@RestController
public class CouponControllerImpl implements CouponController {

    private static final Logger LOG = LoggerFactory.getLogger(CouponControllerImpl.class);

    private final CouponService couponService;

    CouponControllerImpl(CouponService couponService) {
        this.couponService = couponService;
    }


    @PostMapping("/coupons")
    public ResponseEntity<CouponData> createCoupon(@RequestBody @Valid CreateCouponRequest createCouponRequest) {
        LOG.debug("Create coupon request: {}", createCouponRequest);
        CouponData newCouponData = couponService.save(createCouponRequest);
        LOG.debug("Successfully created new coupon: {}", newCouponData);
        return new ResponseEntity<>(newCouponData, HttpStatus.CREATED);
    }

    @GetMapping("/coupons/{code}")
    public ResponseEntity<CouponData> getCoupon(@PathVariable String code) {
        LOG.debug("Get coupon by code: {}", code);
        CouponData couponData = couponService.findByCode(code);
        LOG.debug("Returning coupon by code: {}", couponData);
        return new ResponseEntity<>(couponData, HttpStatus.OK);
    }

    @GetMapping("/coupons")
    public ResponseEntity<Page<CouponData>> getAllCoupons(@RequestParam(required = false, defaultValue = "0") int page,
                                                          @RequestParam(required = false, defaultValue = "#{T(java.lang.Integer).MAX_VALUE}") int size) {
        Page<CouponData> couponDataPage = couponService.findAll(page, size);
        LOG.debug("Returning coupons: {}", couponDataPage);
        return new ResponseEntity<>(couponDataPage, HttpStatus.OK);
    }

    @PostMapping("/coupons/{code}/apply")
    public ResponseEntity<CouponData> apply(@PathVariable String code,
                                            @RequestBody(required = false) ApplyCouponRequest applyCouponRequest) {
        LOG.debug(format("Applying coupon code: %s with request: %s", code, applyCouponRequest));
        CouponData couponData = couponService.applyCoupon(code, applyCouponRequest);
        return new ResponseEntity<>(couponData, HttpStatus.OK);
    }
}
