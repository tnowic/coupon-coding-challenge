package pl.nowito.coupon.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ApplyCouponRequest(
        @NotNull
        @Min(value = 0L)
        Long customerId) {
}
