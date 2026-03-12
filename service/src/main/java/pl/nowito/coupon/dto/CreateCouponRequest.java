package pl.nowito.coupon.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCouponRequest(

        @NotNull
        @Size(min = 1, max = 20)
        @Pattern(regexp = "^[a-zA-Z0-9]*$")
        String code,

        @NotNull
        @Min(value = 0L)
        int maxCounter,

        @Size(min = 2, max = 2)
        @Pattern(regexp = "^[a-zA-Z]*$")
        String countryCode,

        boolean isForRegUsers) {
}
