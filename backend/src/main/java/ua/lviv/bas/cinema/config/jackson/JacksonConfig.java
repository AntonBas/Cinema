package ua.lviv.bas.cinema.config.jackson;

import java.io.IOException;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

@Configuration
public class JacksonConfig {

	@Bean
	Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
		return builder -> builder.deserializerByType(String.class, new StdDeserializer<String>(String.class) {
			@Override
			public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
				String value = p.getValueAsString();
				return value != null ? value.trim() : null;
			}
		});
	}
}