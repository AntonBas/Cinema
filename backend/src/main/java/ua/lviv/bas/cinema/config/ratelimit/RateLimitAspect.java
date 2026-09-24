package ua.lviv.bas.cinema.config.ratelimit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private RateLimitConfig.RateLimitService rateLimitService;

    @Value("${app.rate-limit.client-ip-header:}")
    private String clientIpHeader;

    @Around("@annotation(rateLimit)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = resolveKey(rateLimit.key());

        boolean consumed = rateLimitService.tryConsume(key, 1, rateLimit.value(), rateLimit.duration());

        if (!consumed) {
            HttpServletResponse response = getResponse();
            if (response != null) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setHeader("X-Rate-Limit-Limit", String.valueOf(rateLimit.value()));
                response.setHeader("X-Rate-Limit-Remaining", "0");
                response.setHeader("Retry-After", String.valueOf(rateLimit.duration()));
            }
            return null;
        }

        return joinPoint.proceed();
    }

    private String resolveKey(String keyExpression) {
        HttpServletRequest request = getRequest();

        if (request == null) {
            return "unknown";
        }

        if ("user".equals(keyExpression)) {
            var principal = request.getUserPrincipal();
            return principal != null ? principal.getName() : "anonymous";
        }

        return resolveClientIp(request);
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (StringUtils.hasText(clientIpHeader)) {
            String headerValue = request.getHeader(clientIpHeader);
            if (StringUtils.hasText(headerValue)) {
                return headerValue.trim();
            }
        }
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "unknown";
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private HttpServletResponse getResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getResponse() : null;
    }
}