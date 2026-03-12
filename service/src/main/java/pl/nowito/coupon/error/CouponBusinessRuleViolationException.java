package pl.nowito.coupon.error;

import pl.nowito.coupon.error.support.ErrorMessageSupportEnum;

import java.util.List;

public class CouponBusinessRuleViolationException extends RuntimeException {

    private final ErrorMessageSupportEnum errorMessageEnum;
    private final Object[] params;

    public CouponBusinessRuleViolationException(ErrorMessageSupportEnum errorMessageEnum, Object... params) {
        this.errorMessageEnum = errorMessageEnum;
        this.params = params;
    }

    public String getErrorCode() {
        return errorMessageEnum.name();
    }

    public List<Object> getParams() {
        return List.of(params);
    }

    @Override
    public String getMessage() {
        return "Business rule violation. " + errorMessageEnum.renderErrorMessage(params);
    }

    public String getSuggestion() {
        return errorMessageEnum.getSuggestion();
    }

}
