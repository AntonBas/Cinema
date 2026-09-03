package ua.lviv.bas.cinema.config.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Configuration
public class RateLimitConfig {

    private static final Duration BUCKET_IDLE_EVICTION_THRESHOLD = Duration.ofMinutes(10);

    private final Map<String, BucketEntry> buckets = new ConcurrentHashMap<>();

    @Bean
    RateLimitService rateLimitService() {
        return new RateLimitService(buckets);
    }

    @Scheduled(fixedRateString = "${ratelimit.bucket-eviction-interval:300000}")
    void evictIdleBuckets() {
        Instant cutoff = Instant.now().minus(BUCKET_IDLE_EVICTION_THRESHOLD);
        buckets.values().removeIf(entry -> entry.lastAccessedAt.get().isBefore(cutoff));
    }

    private static final class BucketEntry {

        private final Bucket bucket;
        private final AtomicReference<Instant> lastAccessedAt;

        private BucketEntry(Bucket bucket) {
            this.bucket = bucket;
            this.lastAccessedAt = new AtomicReference<>(Instant.now());
        }
    }

    public static class RateLimitService {

        private final Map<String, BucketEntry> buckets;

        public RateLimitService(Map<String, BucketEntry> buckets) {
            this.buckets = buckets;
        }

        public boolean tryConsume(String key, int tokens, int capacity, int durationInSeconds) {
            String bucketKey = key + ":" + capacity + ":" + durationInSeconds;

            BucketEntry entry = buckets.computeIfAbsent(bucketKey, k -> new BucketEntry(Bucket.builder().addLimit(
                            limit -> limit.capacity(capacity).refillIntervally(capacity, Duration.ofSeconds(durationInSeconds)))
                    .build()));
            entry.lastAccessedAt.set(Instant.now());

            ConsumptionProbe probe = entry.bucket.tryConsumeAndReturnRemaining(tokens);
            return probe.isConsumed();
        }
    }
}