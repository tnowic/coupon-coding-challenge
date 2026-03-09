package pl.nowito.coupon.dto;

import java.sql.Timestamp;

public record CouponData(
        Long id,
        String code,
        Timestamp createTimestamp,
        int maxCounter,
        int counter,
        String countryCode,
        boolean isForRegUsers) {
}
