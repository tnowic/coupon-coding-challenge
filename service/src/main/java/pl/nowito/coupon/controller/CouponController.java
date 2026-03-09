package pl.nowito.coupon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Coupon Controller", description = "Coupon controller exposing rest API")
public class CouponController {

    private static final Logger LOG = LoggerFactory.getLogger(CouponController.class);

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }


    @Operation(summary = "Create a new coupon")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Coupon created successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CouponData.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request or failed request validation",
                    content = {@Content(mediaType = "application/json")})})
    @PostMapping("/coupons")
    ResponseEntity<CouponData> createCoupon(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "New coupon data", required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CreateCouponRequest.class)))
                                            @RequestBody @Valid CreateCouponRequest createCouponRequest) {
        LOG.debug("Create coupon request: {}", createCouponRequest);
        CouponData newCouponData = couponService.save(createCouponRequest);
        LOG.debug("Successfully created new coupon: {}", newCouponData);
        return new ResponseEntity<>(newCouponData, HttpStatus.CREATED);
    }

    @Operation(summary = "Get data for a single coupon")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved coupon with requested code",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CouponData.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request or failed request validation",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Coupon with requested code not found",
                    content = {@Content(mediaType = "application/json")})})
    @GetMapping("/coupons/{code}")
    ResponseEntity<CouponData> getCoupon(@PathVariable String code) {
        LOG.debug("Get coupon by code: {}", code);
        CouponData couponData = couponService.findByCode(code);
        LOG.debug("Returning coupon by code: {}", couponData);
        return new ResponseEntity<>(couponData, HttpStatus.OK);
    }

    @Operation(summary = "Get all coupons data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved coupons",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request or failed request validation",
                    content = {@Content(mediaType = "application/json")})})
    @GetMapping("/coupons")
    ResponseEntity<Page<CouponData>> getAllCoupons(@RequestParam(required = false, defaultValue = "0") int page,
                                                   @RequestParam(required = false, defaultValue = "#{T(java.lang.Integer).MAX_VALUE}") int size) {
        Page<CouponData> couponDataPage = couponService.findAll(page, size);
        LOG.debug("Returning coupons: {}", couponDataPage);
        return new ResponseEntity<>(couponDataPage, HttpStatus.OK);
    }


    @Operation(summary = "Apply a coupon")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully applied coupon",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CouponData.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request or failed request validation",
                    content = {@Content(mediaType = "application/json")})})
    @PostMapping("/coupons/{code}/apply")
    ResponseEntity<CouponData> apply(@PathVariable String code, @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Contains id of a customer who wants to apply this coupon",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApplyCouponRequest.class)))
    @RequestBody(required = false) ApplyCouponRequest applyCouponRequest) {
        LOG.debug(format("Applying coupon code: %s with request: %s", code, applyCouponRequest));
        CouponData couponData = couponService.applyCoupon(code, applyCouponRequest);
        return new ResponseEntity<>(couponData, HttpStatus.OK);
    }
}
