package pl.nowito.coupon.error;

public class CouponNotFoundException extends RuntimeException {

    private final String couponCode;

    public CouponNotFoundException(String couponCode) {
        this.couponCode = couponCode;
    }

    public String getCouponCode() {
        return couponCode;
    }
}
