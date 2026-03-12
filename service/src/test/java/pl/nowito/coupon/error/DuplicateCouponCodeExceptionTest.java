package pl.nowito.coupon.error;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DuplicateCouponCodeExceptionTest {

    @Test
    void testGetMessage() {

        DuplicateCouponCodeException e = new DuplicateCouponCodeException("COUPONDUP");

        assertEquals("Business rule violation. Duplicate coupon code: COUPONDUP", e.getMessage());
        assertEquals("Please use another coupon code", e.getSuggestion());
        assertEquals("ERROR_DUPLICATE_COUPON_CODE", e.getErrorCode());
        assertEquals(List.of("COUPONDUP"), e.getParams());
    }

}