package ua.lviv.bas.cinema.dto.session.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public record SessionUpdateRequest(
		@Schema(description = "Start time of the movie session", example = "2024-01-15T19:00:00") LocalDateTime startTime,

		@Schema(description = "Base price for a standard seat", example = "160.00") BigDecimal basePrice,

		@Schema(description = "ID of the movie being shown", example = "1") Long movieId,

		@Schema(description = "ID of the cinema hall", example = "2") Long hallId) {
}