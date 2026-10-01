package ua.lviv.bas.cinema.config.ratelimit;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitServiceRedisIntegrationTest {

    private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    private static RedisClient clientA;
    private static RedisClient clientB;
    private static StatefulRedisConnection<String, byte[]> connectionA;
    private static StatefulRedisConnection<String, byte[]> connectionB;
    private static RateLimitService instanceA;
    private static RateLimitService instanceB;

    @BeforeAll
    static void setUp() {
        REDIS.start();
        String redisUrl = "redis://" + REDIS.getHost() + ":" + REDIS.getMappedPort(6379);
        var codec = RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE);
        clientA = RedisClient.create(redisUrl);
        clientB = RedisClient.create(redisUrl);
        connectionA = clientA.connect(codec);
        connectionB = clientB.connect(codec);
        instanceA = new RateLimitService(buildProxyManager(connectionA));
        instanceB = new RateLimitService(buildProxyManager(connectionB));
    }

    @AfterAll
    static void tearDown() {
        connectionA.close();
        connectionB.close();
        clientA.shutdown();
        clientB.shutdown();
        REDIS.stop();
    }

    @SuppressWarnings("deprecation")
    private static ProxyManager<String> buildProxyManager(StatefulRedisConnection<String, byte[]> connection) {
        return LettuceBasedProxyManager.builderFor(connection)
                .withExpirationStrategy(
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofHours(1)))
                .build();
    }

    @Test
    void sharesRateLimitStateAcrossApplicationInstances() {
        String key = "login:203.0.113.5";

        assertThat(instanceA.tryConsume(key, 1, 2, 60)).isTrue();
        assertThat(instanceB.tryConsume(key, 1, 2, 60)).isTrue();

        assertThat(instanceA.tryConsume(key, 1, 2, 60)).isFalse();
        assertThat(instanceB.tryConsume(key, 1, 2, 60)).isFalse();
    }

    @Test
    void setsExpirationOnBucketKeysInRedis() {
        instanceA.tryConsume("login:203.0.113.9", 1, 5, 60);

        Long ttlSeconds = connectionA.sync().ttl("login:203.0.113.9:5:60");
        assertThat(ttlSeconds).isPositive();
    }
}
