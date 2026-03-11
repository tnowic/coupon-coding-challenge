package pl.nowito.coupon.converter;

import org.junit.jupiter.api.Test;
import pl.nowito.coupon.dto.CreateCouponRequest;
import pl.nowito.coupon.model.Coupon;

import static org.junit.jupiter.api.Assertions.*;

class CreateCouponRequestConverterTest {

    @Test
    void testConvertToNewEntity() {
        CreateCouponRequest dto = new CreateCouponRequest("TESTCOUPON", 100, "PL", true);

        Coupon c = CreateCouponRequestConverter.convertToNewEntity(dto);

        assertNotNull(c);
        assertEquals("TESTCOUPON", c.getCode());
        assertNotNull(c.getCreateTimestamp());
        assertEquals(100, c.getMaxCounter());
        assertEquals(0, c.getCounter());
        assertEquals("PL", c.getCountryCode());
        assertTrue(c.isForRegUsers());
    }

    @Test
    void testConvertToNewEntityEmptyCountryCode() {
        CreateCouponRequest dto = new CreateCouponRequest("COUPONTEST", 10, null, false);

        Coupon c = CreateCouponRequestConverter.convertToNewEntity(dto);

        assertNotNull(c);
        assertEquals("COUPONTEST", c.getCode());
        assertNotNull(c.getCreateTimestamp());
        assertEquals(10, c.getMaxCounter());
        assertEquals(0, c.getCounter());
        assertNull(c.getCountryCode());
        assertFalse(c.isForRegUsers());
    }

}