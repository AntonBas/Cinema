package ua.lviv.bas.cinema.config.ratelimit;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitRedisConfigTest {

    private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @BeforeAll
    static void startRedis() {
        REDIS.start();
    }

    @AfterAll
    static void stopRedis() {
        REDIS.stop();
    }

    @Configuration
    static class RedisPropertiesConfig {
        @Bean
        DataRedisProperties dataRedisProperties() {
            var properties = new DataRedisProperties();
            properties.setHost(REDIS.getHost());
            properties.setPort(REDIS.getMappedPort(6379));
            return properties;
        }
    }

    @Test
    void wiresUpWorkingRedisProxyManager() {
        new ApplicationContextRunner()
                .withPropertyValues("spring.profiles.active=")
                .withUserConfiguration(RedisPropertiesConfig.class, RateLimitRedisConfig.class)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(ProxyManager.class);

                    @SuppressWarnings("unchecked")
                    var proxyManager = (ProxyManager<String>) context.getBean(ProxyManager.class);
                    var rateLimitService = new RateLimitService(proxyManager);

                    assertThat(rateLimitService.tryConsume("login:198.51.100.7", 1, 1, 60)).isTrue();
                    assertThat(rateLimitService.tryConsume("login:198.51.100.7", 1, 1, 60)).isFalse();
                });
    }
}
