package pl.nowito.coupon.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Service
public class IpResolverService {
    private static final Logger LOG = LoggerFactory.getLogger(IpResolverService.class);

    private static final String COUNTRY_CODE_FIELD_NAME = "countryCode";
    private static final String QUERY_FIELD_NAME = "query";

    private static final TypeReference<Map<String, String>> MAP_TYPE_REFERENCE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final String ipApiUrl;


    public IpResolverService(ObjectMapper objectMapper,
                             RestTemplate restTemplate,
                             @Value("${ip-api.url}") String ipApiUrl) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.ipApiUrl = ipApiUrl;
    }

    public Optional<String> getCountryCodeForIpAddr(String ipAddr) {
        String actualIpApiUrl = String.format("%s/%s?fields=%s,%s", ipApiUrl, ipAddr, COUNTRY_CODE_FIELD_NAME, QUERY_FIELD_NAME);
        LOG.debug("Calling ip-api service url: {}", actualIpApiUrl);

        ResponseEntity<String> response = restTemplate.getForEntity(actualIpApiUrl, String.class);
        try {
            Map<String, String> responseAsMap = objectMapper.readValue(response.getBody(), MAP_TYPE_REFERENCE);
            String countryCode = responseAsMap.get(COUNTRY_CODE_FIELD_NAME);
            String publicIpAddr = responseAsMap.get(QUERY_FIELD_NAME);
            LOG.debug("Country code: {} for ip: {} from ip-api service", countryCode, publicIpAddr);
            if (countryCode == null) {
                LOG.warn("Country code from request is null. Returning empty country code");
                return Optional.empty();
            }
            return Optional.of(countryCode);
        } catch (Exception e) {
            LOG.warn("Exception thrown while retrieving country code for http request. Returning empty country code", e);
            return Optional.empty();
        }

    }

}
