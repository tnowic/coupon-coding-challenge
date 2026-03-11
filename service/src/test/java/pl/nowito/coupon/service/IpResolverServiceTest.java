package pl.nowito.coupon.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class IpResolverServiceTest {

    private static final String IP_API_URL = "http://test-ip-api-url";

    private static final String TEST_RESPONSE_FROM_IP_API = """
            {
                "countryCode": "PL",
                "query": "217.119.72.129"
            }
            """;
    private static final String TEST_RESPONSE_FROM_IP_API_NOT_FOUND = """
            {
                "query": "127.0.0.1"
            }
            """;

    @Mock
    private RestTemplate restTemplateMock;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    void testGetCountryCodeForIpAddr() {
        when(restTemplateMock.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(TEST_RESPONSE_FROM_IP_API, HttpStatus.OK));

        IpResolverService service = new IpResolverService(objectMapper, restTemplateMock, IP_API_URL);
        Optional<String> countryCode = service.getCountryCodeForIpAddr("217.119.72.129");

        assertTrue(countryCode.isPresent());
        assertEquals("PL", countryCode.get());
    }

    @Test
    void testGetCountryCodeForIpAddrNotFoud() {
        when(restTemplateMock.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(TEST_RESPONSE_FROM_IP_API_NOT_FOUND, HttpStatus.OK));

        IpResolverService service = new IpResolverService(objectMapper, restTemplateMock, IP_API_URL);
        Optional<String> countryCode = service.getCountryCodeForIpAddr("127.0.0.1");

        assertTrue(countryCode.isEmpty());
    }

    @Test
    void testGetCountryCodeForIpAddrResourceAccessException() {
        when(restTemplateMock.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new ResourceAccessException("TEST: I/O error on GET request"));

        IpResolverService service = new IpResolverService(objectMapper, restTemplateMock, IP_API_URL);
        Optional<String> countryCode = service.getCountryCodeForIpAddr("127.0.0.1");

        assertTrue(countryCode.isEmpty());
    }
}