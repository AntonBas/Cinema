package ua.lviv.bas.cinema.config.cache;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	CacheManager cacheManager() {
		CaffeineCacheManager cacheManager = new CaffeineCacheManager();

		cacheManager.registerCustomCache("seats",
				Caffeine.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).maximumSize(1000).build());

		cacheManager.registerCustomCache("cinemaHalls",
				Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).maximumSize(100).build());

		cacheManager.registerCustomCache("seatAvailability",
				Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(500).build());

		cacheManager.registerCustomCache("availableSeatsCount",
				Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(500).build());

		cacheManager.registerCustomCache("ticket-types",
				Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).maximumSize(100).build());

		cacheManager.registerCustomCache("sessions",
				Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(500).build());

		cacheManager.registerCustomCache("users",
				Caffeine.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).maximumSize(1000).build());

		cacheManager.registerCustomCache("genres",
				Caffeine.newBuilder().expireAfterWrite(24, TimeUnit.HOURS).maximumSize(50).build());

		cacheManager.registerCustomCache("persons",
				Caffeine.newBuilder().expireAfterWrite(24, TimeUnit.HOURS).maximumSize(500).build());

		cacheManager.setCaffeine(defaultCaffeineConfig());

		return cacheManager;
	}

	private Caffeine<Object, Object> defaultCaffeineConfig() {
		return Caffeine.newBuilder().initialCapacity(100).maximumSize(500).expireAfterWrite(30, TimeUnit.MINUTES)
				.recordStats();
	}
}