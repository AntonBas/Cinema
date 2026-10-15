package ua.lviv.bas.cinema.config.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ua.lviv.bas.cinema.exception.infrastructure.RateLimitExceededException;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private RateLimitService rateLimitService;

    @Value("${app.rate-limit.client-ip-header:}")
    private String clientIpHeader;

    @Around("@annotation(rateLimit)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        var signature = joinPoint.getSignature();
        String key = signature.getDeclaringType().getSimpleName() + "." + signature.getName() + ":"
                + resolveKey(rateLimit.key());

        if (!rateLimitService.tryConsume(key, 1, rateLimit.value(), rateLimit.duration())) {
            throw new RateLimitExceededException(rateLimit.value(), rateLimit.duration());
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
        if (log.isDebugEnabled()) {
            log.debug("Client IP candidates: remoteAddr={}, X-Forwarded-For={}, X-Real-IP={}, True-Client-IP={}, "
                            + "CF-Connecting-IP={}, X-Vercel-Forwarded-For={}", request.getRemoteAddr(),
                    request.getHeader("X-Forwarded-For"), request.getHeader("X-Real-IP"),
                    request.getHeader("True-Client-IP"), request.getHeader("CF-Connecting-IP"),
                    request.getHeader("X-Vercel-Forwarded-For"));
        }
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
}