package ua.lviv.bas.cinema.refund.dto.response;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RefundResponseJsonTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void instantFieldsShouldSerializeInCinemaTimeZone() {
        RefundResponse response = new RefundResponse(1L, "RF-2026-0001", "PROCESSED", new BigDecimal("180.00"), 90,
                "Cannot attend", "AUTO_SYSTEM", Instant.parse("2026-10-14T10:20:00Z"),
                Instant.parse("2026-01-15T08:15:00Z"), 456L, "CARD", null, "Refund processed", "3-5 business days");

        JsonNode json = jsonMapper.readTree(jsonMapper.writeValueAsString(response));

        assertThat(json.get("processedAt").asString()).isEqualTo("14.10.2026 13:20");
        assertThat(json.get("createdAt").asString()).isEqualTo("15.01.2026 10:15");
    }
}
