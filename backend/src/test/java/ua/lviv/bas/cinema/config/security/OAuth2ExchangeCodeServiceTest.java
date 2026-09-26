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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2ExchangeCodeServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private OAuth2ExchangeCodeService exchangeCodeService;

    @Test
    void issueCodeShouldStoreEmailWithSixtySecondTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        String code = exchangeCodeService.issueCode("user@example.com");

        assertThat(code).isNotBlank();
        verify(valueOperations).set(eq("oauth2:exchange:" + code), eq("user@example.com"), eq(Duration.ofSeconds(60)));
    }

    @Test
    void consumeShouldReturnEmailAndDeleteKeyWhenPresent() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.getAndDelete("oauth2:exchange:some-code")).thenReturn("user@example.com");

        var result = exchangeCodeService.consume("some-code");

        assertThat(result).contains("user@example.com");
    }

    @Test
    void consumeShouldReturnEmptyWhenCodeUnknownOrAlreadyUsed() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.getAndDelete(any())).thenReturn(null);

        var result = exchangeCodeService.consume("unknown-code");

        assertThat(result).isEmpty();
    }
}
