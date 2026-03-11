package pl.nowito.coupon.user;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.nowito.coupon.service.IpResolverService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class UserContextTest {

    @Mock
    private IpResolverService ipResolverServiceMock;
    @Mock
    private HttpServletRequest requestMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetIpFromRequestHeader() {
        String testIp = "192.168.1.1";
        when(requestMock.getHeader("X-Forwarded-For")).thenReturn(testIp);

        UserContext userContext = new UserContext(requestMock, ipResolverServiceMock);

        assertEquals(testIp, userContext.getIpFromRequest(requestMock));
    }

    @Test
    void testGetIpFromRequestRemoteAddr() {
        String testIp = "127.0.0.1";
        when(requestMock.getRemoteAddr()).thenReturn(testIp);

        UserContext userContext = new UserContext(requestMock, ipResolverServiceMock);

        assertEquals(testIp, userContext.getIpFromRequest(requestMock));
    }

    @Test
    void testGetRequestOriginCountryCode() {
        String testCountryCode = "PL";
        String testIp = "192.168.1.1";
        when(requestMock.getRemoteAddr()).thenReturn(testIp);
        when(ipResolverServiceMock.getCountryCodeForIpAddr(testIp)).thenReturn(Optional.of(testCountryCode));

        UserContext userContext = new UserContext(requestMock, ipResolverServiceMock);

        assertTrue(userContext.getRequestOriginCountryCode().isPresent());
        assertEquals(testCountryCode, userContext.getRequestOriginCountryCode().get());
    }
}