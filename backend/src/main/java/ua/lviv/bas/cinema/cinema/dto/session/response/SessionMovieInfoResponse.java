package ua.lviv.bas.cinema.cinema.dto.session.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Session info for movie detail page")
public record SessionMovieInfoResponse(
        @Schema(description = "Unique identifier of the session", example = "1")
        Long id,

        @Schema(description = "Public identifier of the session, used in customer-facing URLs",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID publicId,

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