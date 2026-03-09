package pl.nowito.coupon.user;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import pl.nowito.coupon.service.IpResolverService;

import java.util.Optional;

@Component
@RequestScope
public class UserContext {

    private static final Logger LOG = LoggerFactory.getLogger(UserContext.class);

    private final HttpServletRequest request;
    private final IpResolverService ipResolverService;


    public UserContext(HttpServletRequest request, IpResolverService ipResolverService) {
        this.request = request;
        this.ipResolverService = ipResolverService;
    }

    public Optional<String> getRequestOriginCountryCode() {
        return ipResolverService.getCountryCodeForIpAddr(extractIp(request));
    }

    private String extractIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        LOG.debug("X-Forwarded-For: {} " , xForwardedFor);
        return !StringUtils.isBlank(xForwardedFor) ? xForwardedFor.split(",")[0].trim() : request.getRemoteAddr();
    }




}