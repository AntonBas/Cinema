package ua.lviv.bas.cinema.config.ratelimit;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RateLimitService {

    private final ProxyManager<String> proxyManager;

    public boolean tryConsume(String key, int tokens, int capacity, int durationInSeconds) {
        String bucketKey = key + ":" + capacity + ":" + durationInSeconds;

        var configuration = BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(capacity)
                        .refillIntervally(capacity, Duration.ofSeconds(durationInSeconds)))
                .build();

        var bucket = proxyManager.builder().build(bucketKey, () -> configuration);
        return bucket.tryConsume(tokens);
    }
}
