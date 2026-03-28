package ua.lviv.bas.cinema.config.ratelimit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Aspect
@Component
public class RateLimitAspect {

	@Autowired
	private RateLimitConfig.RateLimitService rateLimitService;

	@Around("@annotation(rateLimit)")
	public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
		String key = resolveKey(rateLimit.key());

		if (!rateLimitService.tryConsume(key, 1)) {
			HttpServletResponse response = getResponse();
			response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			response.setHeader("X-Rate-Limit-Limit", String.valueOf(rateLimit.value()));
			response.setHeader("X-Rate-Limit-Remaining", "0");
			response.setHeader("Retry-After", String.valueOf(rateLimit.duration() * 60));
			return null;
		}

		return joinPoint.proceed();
	}

	private String resolveKey(String keyExpression) {
		HttpServletRequest request = getRequest();

		if ("ip".equals(keyExpression)) {
			String xForwardedFor = request.getHeader("X-Forwarded-For");
			if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
				return xForwardedFor.split(",")[0].trim();
			}
			return request.getRemoteAddr();
		}

		if ("user".equals(keyExpression)) {
			return request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";
		}

		return request.getRemoteAddr();
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