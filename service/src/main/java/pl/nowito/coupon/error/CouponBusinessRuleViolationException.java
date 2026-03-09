package pl.nowito.coupon.error;

public class CouponBusinessRuleViolationException extends RuntimeException {

    private final String errorMessage;
    private final String suggestion;

    public CouponBusinessRuleViolationException(String message, String suggestion) {
        this.errorMessage = message;
        this.suggestion = suggestion;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
