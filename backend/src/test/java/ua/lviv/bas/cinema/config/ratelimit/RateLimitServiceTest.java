package ua.lviv.bas.cinema.config.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService(new InMemoryProxyManager());
    }

    @Test
    void allowsRequestsUpToCapacityThenRejects() {
        assertThat(rateLimitService.tryConsume("login:198.51.100.1", 1, 2, 60)).isTrue();
        assertThat(rateLimitService.tryConsume("login:198.51.100.1", 1, 2, 60)).isTrue();
        assertThat(rateLimitService.tryConsume("login:198.51.100.1", 1, 2, 60)).isFalse();
    }

    @Test
    void keepsSeparateBucketsForDifferentKeys() {
        assertThat(rateLimitService.tryConsume("login:198.51.100.1", 1, 1, 60)).isTrue();
        assertThat(rateLimitService.tryConsume("login:198.51.100.1", 1, 1, 60)).isFalse();

        assertThat(rateLimitService.tryConsume("login:198.51.100.2", 1, 1, 60)).isTrue();
    }

    @Test
    void keepsSeparateBucketsForDifferentLimitsOnSameKey() {
        assertThat(rateLimitService.tryConsume("hold:user-1", 1, 1, 60)).isTrue();
        assertThat(rateLimitService.tryConsume("hold:user-1", 1, 1, 60)).isFalse();

        assertThat(rateLimitService.tryConsume("hold:user-1", 1, 30, 60)).isTrue();
    }
}
