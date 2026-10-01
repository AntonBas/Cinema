package ua.lviv.bas.cinema.config.ratelimit;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

@Configuration
@Profile("!ci")
public class RateLimitRedisConfig {

    private static final Duration MAX_BUCKET_TTL = Duration.ofHours(1);

    @Bean(destroyMethod = "shutdown")
    public RedisClient rateLimitRedisClient(DataRedisProperties properties) {
        var uri = properties.getUrl() != null
                ? RedisURI.create(properties.getUrl())
                : buildStandaloneUri(properties);
        return RedisClient.create(uri);
    }

    private RedisURI buildStandaloneUri(DataRedisProperties properties) {
        var builder = RedisURI.Builder.redis(properties.getHost(), properties.getPort())
                .withDatabase(properties.getDatabase());
        if (properties.getPassword() != null) {
            builder.withPassword(properties.getPassword().toCharArray());
        }
        return builder.build();
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, byte[]> rateLimitRedisConnection(RedisClient rateLimitRedisClient) {
        return rateLimitRedisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
    }

    @Bean
    @SuppressWarnings("deprecation")
    public ProxyManager<String> rateLimitProxyManager(StatefulRedisConnection<String, byte[]> rateLimitRedisConnection) {
        return LettuceBasedProxyManager.builderFor(rateLimitRedisConnection)
                .withExpirationStrategy(
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(MAX_BUCKET_TTL))
                .build();
    }
}
