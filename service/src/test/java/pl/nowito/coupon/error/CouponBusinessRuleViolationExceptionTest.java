package pl.nowito.coupon.error;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pl.nowito.coupon.error.support.ErrorMessageSupportEnum.ERROR_COUPON_MAX_COUNTER_REACHED;

class CouponBusinessRuleViolationExceptionTest {

    @Test
    void testGetMessage() {

        CouponBusinessRuleViolationException e = new CouponBusinessRuleViolationException(ERROR_COUPON_MAX_COUNTER_REACHED, "NEWCOUPON", 10);

        assertEquals("Business rule violation. A counter of coupon code: NEWCOUPON reached its maximum possible value: 10", e.getMessage());
        assertEquals("Please use another coupon code to apply", e.getSuggestion());
        assertEquals("ERROR_COUPON_MAX_COUNTER_REACHED", e.getErrorCode());
        assertEquals(List.of("NEWCOUPON", 10), e.getParams());
    }

}