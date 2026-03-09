package pl.nowito.coupon.error;

public class CouponNotFoundException extends RuntimeException {

    private final String code;

    public CouponNotFoundException(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
