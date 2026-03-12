package pl.nowito.coupon.converter;

import pl.nowito.coupon.dto.CreateCouponRequest;
import pl.nowito.coupon.model.Coupon;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class CreateCouponRequestConverter {

    private CreateCouponRequestConverter() {
    }

    public static Coupon convertToNewEntity(CreateCouponRequest dto) {
        Coupon c = new Coupon();
        c.setCode(dto.code().toUpperCase());
        c.setMaxCounter(dto.maxCounter());
        c.setCounter(0);
        c.setCreateTimestamp(Timestamp.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)));
        if (dto.countryCode() != null) {
            c.setCountryCode(dto.countryCode().toUpperCase());
        }
        c.setForRegUsers(dto.isForRegUsers());
        return c;
    }
}

