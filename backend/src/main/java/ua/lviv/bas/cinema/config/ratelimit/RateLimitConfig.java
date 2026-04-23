package ua.lviv.bas.cinema.config.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Bean
    RateLimitService rateLimitService() {
        return new RateLimitService(buckets);
    }

    public static class RateLimitService {

        private final Map<String, Bucket> buckets;

        public RateLimitService(Map<String, Bucket> buckets) {
            this.buckets = buckets;
        }

        public boolean tryConsume(String key, int tokens, int capacity, int durationInMinutes) {
            String bucketKey = key + ":" + capacity + ":" + durationInMinutes;

            Bucket bucket = buckets.computeIfAbsent(bucketKey, k -> Bucket.builder().addLimit(
                            limit -> limit.capacity(capacity).refillIntervally(capacity, Duration.ofMinutes(durationInMinutes)))
                    .build());

            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(tokens);
            return probe.isConsumed();
        }
    }
}