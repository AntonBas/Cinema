package ua.lviv.bas.cinema.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuth2ExchangeCodeService {

    private static final String KEY_PREFIX = "oauth2:exchange:";
    private static final Duration TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;

    public String issueCode(String email) {
        String code = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(KEY_PREFIX + code, email, TTL);
        return code;
    }

    public Optional<String> consume(String code) {
        String email = redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + code);
        return Optional.ofNullable(email);
    }
}
