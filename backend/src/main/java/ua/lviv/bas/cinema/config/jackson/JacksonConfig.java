package ua.lviv.bas.cinema.config.jackson;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.module.SimpleModule;

@Configuration
public class JacksonConfig {

    @Bean
    JsonMapperBuilderCustomizer stringTrimmingJsonMapperBuilderCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addDeserializer(String.class, new StringTrimmingDeserializer());
            builder.addModule(module);
        };
    }

    private static final class StringTrimmingDeserializer extends ValueDeserializer<String> {

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            String value = p.getValueAsString();
            return value != null ? value.trim() : null;
        }
    }
}
