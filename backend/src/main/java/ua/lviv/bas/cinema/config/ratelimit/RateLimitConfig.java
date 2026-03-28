package ua.lviv.bas.cinema.config.ratelimit;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

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

		public boolean tryConsume(String key, int tokens) {
			Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket());
			ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(tokens);
			return probe.isConsumed();
		}

		private Bucket createBucket() {
			return Bucket.builder().addLimit(limit -> limit.capacity(5).refillIntervally(5, Duration.ofMinutes(15)))
					.build();
		}
	}
}