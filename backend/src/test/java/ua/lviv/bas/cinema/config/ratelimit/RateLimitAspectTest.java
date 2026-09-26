package ua.lviv.bas.cinema.config.ratelimit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
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
import ua.lviv.bas.cinema.exception.infrastructure.RateLimitExceededException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    private static final String SIGNATURE = "public String AuthController.login(UserLoginRequest)";
    private static final String KEY_PREFIX = SIGNATURE + ":";

    @Mock
    private RateLimitConfig.RateLimitService rateLimitService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    private RateLimitAspect rateLimitAspect;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        rateLimitAspect = new RateLimitAspect();
        ReflectionTestUtils.setField(rateLimitAspect, "rateLimitService", rateLimitService);

        lenient().when(joinPoint.getSignature()).thenReturn(signature);
        lenient().when(signature.toLongString()).thenReturn(SIGNATURE);

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

        when(rateLimitService.tryConsume(eq(KEY_PREFIX + "10.0.0.5"), anyInt(), anyInt(), anyInt())).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("ok");

        rateLimitAspect.checkRateLimit(joinPoint, ipRateLimit());

        verify(rateLimitService).tryConsume(eq(KEY_PREFIX + "10.0.0.5"), anyInt(), anyInt(), anyInt());
    }

    @Test
    void checkRateLimitWithIpKeyShouldUseDifferentBucketPerSpoofedHeaderValue() throws Throwable {
        request.setRemoteAddr("10.0.0.5");

        request.addHeader("X-Forwarded-For", "1.1.1.1");
        when(rateLimitService.tryConsume(eq(KEY_PREFIX + "10.0.0.5"), anyInt(), anyInt(), anyInt())).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("ok");
        rateLimitAspect.checkRateLimit(joinPoint, ipRateLimit());

        request.removeHeader("X-Forwarded-For");
        request.addHeader("X-Forwarded-For", "2.2.2.2");
        rateLimitAspect.checkRateLimit(joinPoint, ipRateLimit());

        verify(rateLimitService, org.mockito.Mockito.times(2)).tryConsume(eq(KEY_PREFIX + "10.0.0.5"), anyInt(), anyInt(), anyInt());
    }

    @Test
    void checkRateLimitWithConfiguredClientIpHeaderShouldUseItInsteadOfForwardedFor() throws Throwable {
        ReflectionTestUtils.setField(rateLimitAspect, "clientIpHeader", "True-Client-IP");
        request.setRemoteAddr("1.2.3.4");
        request.addHeader("X-Forwarded-For", "1.2.3.4, 81.97.145.24, 172.71.195.123");
        request.addHeader("True-Client-IP", "81.97.145.24");

        when(rateLimitService.tryConsume(eq(KEY_PREFIX + "81.97.145.24"), anyInt(), anyInt(), anyInt())).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("ok");

        rateLimitAspect.checkRateLimit(joinPoint, ipRateLimit());

        verify(rateLimitService).tryConsume(eq(KEY_PREFIX + "81.97.145.24"), anyInt(), anyInt(), anyInt());
    }

    @Test
    void checkRateLimitWhenConfiguredClientIpHeaderMissingShouldFallBackToRemoteAddr() throws Throwable {
        ReflectionTestUtils.setField(rateLimitAspect, "clientIpHeader", "True-Client-IP");
        request.setRemoteAddr("10.0.0.5");

        when(rateLimitService.tryConsume(eq(KEY_PREFIX + "10.0.0.5"), anyInt(), anyInt(), anyInt())).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("ok");

        rateLimitAspect.checkRateLimit(joinPoint, ipRateLimit());

        verify(rateLimitService).tryConsume(eq(KEY_PREFIX + "10.0.0.5"), anyInt(), anyInt(), anyInt());
    }

    @Test
    void checkRateLimitWhenLimitExceededShouldThrowInsteadOfProceeding() throws Throwable {
        request.setRemoteAddr("10.0.0.5");
        when(rateLimitService.tryConsume(eq(KEY_PREFIX + "10.0.0.5"), anyInt(), anyInt(), anyInt())).thenReturn(false);

        assertThatThrownBy(() -> rateLimitAspect.checkRateLimit(joinPoint, ipRateLimit()))
                .isInstanceOf(RateLimitExceededException.class);
        verify(joinPoint, never()).proceed();
    }

    @Test
    void checkRateLimitShouldUseSeparateBucketPerEndpoint() throws Throwable {
        request.setRemoteAddr("10.0.0.5");
        when(signature.toLongString()).thenReturn("endpointA", "endpointB");
        when(rateLimitService.tryConsume(anyString(), anyInt(), anyInt(), anyInt())).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("ok");

        rateLimitAspect.checkRateLimit(joinPoint, ipRateLimit());
        rateLimitAspect.checkRateLimit(joinPoint, ipRateLimit());

        verify(rateLimitService).tryConsume(eq("endpointA:10.0.0.5"), anyInt(), anyInt(), anyInt());
        verify(rateLimitService).tryConsume(eq("endpointB:10.0.0.5"), anyInt(), anyInt(), anyInt());
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
