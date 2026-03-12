package pl.nowito.coupon.error;

import pl.nowito.coupon.error.support.ErrorMessageSupportEnum;

public class DuplicateCouponCodeException extends CouponBusinessRuleViolationException {

    public DuplicateCouponCodeException(String couponCode) {
        super(ErrorMessageSupportEnum.ERROR_DUPLICATE_COUPON_CODE, couponCode);
    }
}
