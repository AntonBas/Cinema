package ua.lviv.bas.cinema.config.ratelimit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    @Mock
    private RateLimitConfig.RateLimitService rateLimitService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    private RateLimitAspect rateLimitAspect;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        rateLimitAspect = new RateLimitAspect();
        ReflectionTestUtils.setField(rateLimitAspect, "rateLimitService", rateLimitService);

        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void checkRateLimitWithIpKeyShouldIgnoreSpoofedForwardedForHeader() throws Throwable {
        request.setRemoteAddr("10.0.0.5");
        request.addHeader("X-Forwarded-For", "1.2.3.4");

        when(rateLimitService.tryConsume(eq("10.0.0.5"), anyInt(), anyInt(), anyInt())).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("ok");

        rateLimitAspect.checkRateLimit(joinPoint, ipRateLimit());

        verify(rateLimitService).tryConsume(eq("10.0.0.5"), anyInt(), anyInt(), anyInt());
    }

    @Test
    void checkRateLimitWithIpKeyShouldUseDifferentBucketPerSpoofedHeaderValue() throws Throwable {
        request.setRemoteAddr("10.0.0.5");

        request.addHeader("X-Forwarded-For", "1.1.1.1");
        when(rateLimitService.tryConsume(eq("10.0.0.5"), anyInt(), anyInt(), anyInt())).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("ok");
        rateLimitAspect.checkRateLimit(joinPoint, ipRateLimit());

        request.removeHeader("X-Forwarded-For");
        request.addHeader("X-Forwarded-For", "2.2.2.2");
        rateLimitAspect.checkRateLimit(joinPoint, ipRateLimit());

        verify(rateLimitService, org.mockito.Mockito.times(2)).tryConsume(eq("10.0.0.5"), anyInt(), anyInt(), anyInt());
    }

    private RateLimit ipRateLimit() {
        try {
            return Holder.class.getMethod("annotated").getAnnotation(RateLimit.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final class Holder {
        @RateLimit(value = 5, duration = 60, key = "ip")
        public void annotated() {
        }
    }
}
