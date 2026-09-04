package ua.lviv.bas.cinema.config.cache;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(pageAwareJsonSerializer()));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("cinemaHalls", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("sessions", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("genres", defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put("persons", defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put("singleMovies", defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put("movieLists", defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put("seatAvailability", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("tickets", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("ticketTypes", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("userDetails", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("bonusRules", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("bonus", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("promotions", defaultConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    private static RedisSerializer<Object> pageAwareJsonSerializer() {
        return GenericJacksonJsonRedisSerializer.create(it -> it.enableSpringCacheNullValueSupport()
                .enableUnsafeDefaultTyping().customize(builder -> builder.addModule(pageImplModule())));
    }

    private static SimpleModule pageImplModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PageImpl.class, new PageImplDeserializer());
        return module;
    }

    private static final class PageImplDeserializer extends ValueDeserializer<PageImpl<Object>> {

        @Override
        public PageImpl<Object> deserialize(JsonParser parser, DeserializationContext context)
                throws JacksonException {
            JsonNode node = context.readTree(parser);
            List<Object> content = context.readTreeAsValue(node.path("content"),
                    context.getTypeFactory().constructCollectionType(List.class, Object.class));
            int number = node.path("number").asInt();
            int size = node.path("size").asInt();
            long totalElements = node.path("totalElements").asLong();
            return new PageImpl<>(content, PageRequest.of(number, size), totalElements);
        }
    }
}
