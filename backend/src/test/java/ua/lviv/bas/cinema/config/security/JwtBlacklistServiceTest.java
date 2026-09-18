package ua.lviv.bas.cinema.config.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private JwtBlacklistService blacklistService;

    @Test
    void blacklistShouldStoreKeyWithTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        blacklistService.blacklist("jti-123", Duration.ofMinutes(5));

        verify(valueOperations).set(eq("jwt:blacklist:jti-123"), eq("1"), eq(Duration.ofMinutes(5)));
    }

    @Test
    void blacklistShouldDoNothingWhenJtiIsNull() {
        blacklistService.blacklist(null, Duration.ofMinutes(5));

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void blacklistShouldDoNothingWhenTtlIsZeroOrNegative() {
        blacklistService.blacklist("jti-123", Duration.ZERO);
        blacklistService.blacklist("jti-123", Duration.ofSeconds(-5));

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void isBlacklistedShouldReturnTrueWhenKeyExists() {
        when(redisTemplate.hasKey("jwt:blacklist:jti-123")).thenReturn(true);

        assertThat(blacklistService.isBlacklisted("jti-123")).isTrue();
    }

    @Test
    void isBlacklistedShouldReturnFalseWhenKeyAbsent() {
        when(redisTemplate.hasKey("jwt:blacklist:jti-123")).thenReturn(false);

        assertThat(blacklistService.isBlacklisted("jti-123")).isFalse();
    }

    @Test
    void isBlacklistedShouldReturnFalseWhenJtiIsNull() {
        assertThat(blacklistService.isBlacklisted(null)).isFalse();
        verify(redisTemplate, never()).hasKey(any());
    }
}
