package pl.nowito.coupon.converter;

import pl.nowito.coupon.dto.CouponData;
import pl.nowito.coupon.model.Coupon;

public class CouponDataConverter {

    private CouponDataConverter() {
    }


    public static CouponData convertToDto(Coupon coupon) {
        return new CouponData(
                coupon.getId(),
                coupon.getCode(),
                coupon.getCreateTimestamp(),
                coupon.getMaxCounter(),
                coupon.getCounter(),
                coupon.getCountryCode(),
                coupon.isForRegUsers()
        );
    }

}
