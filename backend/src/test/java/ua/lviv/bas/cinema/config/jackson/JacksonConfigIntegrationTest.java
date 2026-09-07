package ua.lviv.bas.cinema.config.jackson;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import tools.jackson.databind.json.JsonMapper;

import ua.lviv.bas.cinema.config.TestcontainersConfig;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("ci")
@Import(TestcontainersConfig.class)
class JacksonConfigIntegrationTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void stringDeserializationShouldTrimLeadingAndTrailingWhitespace() {
        String result = jsonMapper.readValue("\"  padded value  \"", String.class);

        assertThat(result).isEqualTo("padded value");
    }

    @Test
    void localDateTimeSerializationShouldStillUseIsoFormatThroughTheRealJsonMapperBean() {
        var value = LocalDateTime.of(2026, 9, 9, 21, 30, 0);

        String json = jsonMapper.writeValueAsString(value);

        assertThat(json).isEqualTo("\"2026-09-09T21:30:00\"");
    }
}
