package pl.nowito.coupon.error.support;

public enum ErrorMessageSupportEnum {

    ERROR_COUPON_CODE_NOT_FOUND("Coupon code: %s not found", "Please check your code and try again"),
    ERROR_COUPON_FOR_REGISTERED_CUSTOMERS_ONLY("Coupon code: %s is available only for registered customers", "Please provide customer id in request body"),
    ERROR_REGISTERED_CUSTOMER_NOT_FOUND("Registered customer id: %d not found", "Please provide valid customer id in request body"),
    ERROR_CUSTOMER_ALREADY_APPLIED_FOR_COUPON("Customer with id: %s has already applied coupon code: %s", "Please use other coupon code or choose different customer id"),
    ERROR_COUPON_MAX_COUNTER_REACHED("A counter of coupon code: %s reached its maximum possible value: %s", "Please use another coupon code to apply"),
    ERROR_COUNTRY_CODE_FOR_REQUEST_NOT_FOUND("Request origin country could not be determined when applying to coupon code: %s restricted to country code: %s", "Please verify whether valid public ip is being used in your http request"),
    ERROR_COUPON_COUNTRY_CODE_RESTRICTED("Request origin country code: %s differs from coupon restricted country code: %s when applying to coupon code: %s", "Your public ip address must be in a country that this coupon is restricted to"),
    ERROR_DUPLICATE_COUPON_CODE("Duplicate coupon code: %s", "Please use another coupon code"),
    ERROR_DATA_INTEGRITY_VIOLATION("Data integrity violation. %s", "Please contact application support"),
    INTERNAL_SERVER_ERROR("Internal server error occurred", "Please contact application support");

    private final String msgTemplate;
    private final String suggestion;

    ErrorMessageSupportEnum(String msgTemplate, String suggestion) {
        this.msgTemplate = msgTemplate;
        this.suggestion = suggestion;
    }

    public String getMsgTemplate() {
        return msgTemplate;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public String renderErrorMessage(Object[] params) {
        return String.format(msgTemplate, params);
    }

    public String getErrorCode() {
        return name();
    }

}
