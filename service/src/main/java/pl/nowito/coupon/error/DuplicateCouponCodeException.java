package pl.nowito.coupon.error;

public class DuplicateCouponCodeException extends CouponBusinessRuleViolationException {

    public DuplicateCouponCodeException(String code) {
        super("Duplicate coupon code:" + code, "Please use another coupon code");
    }
}
