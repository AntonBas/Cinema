package ua.lviv.bas.cinema.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class JacksonConfig {

    @Bean
    SimpleModule stringTrimModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new StdDeserializer<>(String.class) {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ct) throws IOException {
                String value = p.getValueAsString();
                return value != null ? value.trim() : null;
            }
        });
        return module;
    }
}