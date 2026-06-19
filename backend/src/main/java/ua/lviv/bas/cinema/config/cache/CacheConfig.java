package ua.lviv.bas.cinema.config.cache;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(RedisSerializer.json()));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("seats", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("cinemaHalls", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("sessions", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("genres", defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put("persons", defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put("movies", defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put("seatAvailability", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("availableSeatsCount", defaultConfig.entryTtl(Duration.ofMinutes(1)));
        cacheConfigurations.put("bookings", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("tickets", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("ticket-types", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("adminUsers", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("bonusRules", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("bonus", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("promotions", defaultConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}