package pl.nowito.coupon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import pl.nowito.coupon.dto.ApplyCouponRequest;
import pl.nowito.coupon.dto.CouponData;
import pl.nowito.coupon.dto.CreateCouponRequest;
import pl.nowito.coupon.dto.ErrorResponse;

@Tag(name = "Coupon Controller", description = "Coupon controller exposing rest API")
public interface CouponController {

    @Operation(summary = "Create a new coupon")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Coupon created successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CouponData.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request or failed request validation",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "409", description = "Duplicate coupon code",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))})})
    ResponseEntity<CouponData> createCoupon(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "New coupon data", required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CreateCouponRequest.class)))
                                            @RequestBody @Valid CreateCouponRequest createCouponRequest);

    @Operation(summary = "Get data for a single coupon")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved coupon with requested code",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CouponData.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request or failed request validation",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Coupon with requested code not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))})})
    ResponseEntity<CouponData> getCoupon(@PathVariable String code);

    @Operation(summary = "Get all coupons data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved coupons",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request or failed request validation",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))})})
    ResponseEntity<Page<CouponData>> getAllCoupons(@RequestParam(required = false, defaultValue = "0") int page,
                                                   @RequestParam(required = false, defaultValue = "#{T(java.lang.Integer).MAX_VALUE}") int size);


    @Operation(summary = "Apply a coupon")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully applied coupon",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = CouponData.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request or failed request validation",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Coupon with requested code not found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))})})
    ResponseEntity<CouponData> apply(@PathVariable String code, @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Contains id of a customer who wants to apply this coupon",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApplyCouponRequest.class)))
    @RequestBody(required = false) ApplyCouponRequest applyCouponRequest);
}
