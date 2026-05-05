package ua.lviv.bas.cinema.dto.session.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Session info for movie detail page")
public record SessionMovieInfoResponse(
        @Schema(description = "Unique identifier of the session", example = "1")
        Long id,

        @Schema(description = "Start time of the session", example = "2024-01-15T18:30:00")
        LocalDateTime startTime,

        @Schema(description = "End time of the session", example = "2024-01-15T21:00:00")
        LocalDateTime endTime,

        @Schema(description = "Base price", example = "150.00")
        BigDecimal basePrice,

        @Schema(description = "Available seats", example = "105")
        Integer availableSeats,

        @Schema(description = "Name of the cinema hall", example = "Hall A")
        String hallName
) {
}