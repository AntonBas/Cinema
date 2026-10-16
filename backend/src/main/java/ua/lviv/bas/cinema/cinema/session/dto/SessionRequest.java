package ua.lviv.bas.cinema.cinema.session.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Request to create or update a cinema session")
public record SessionRequest(
        @Schema(description = "Start time of the movie session", example = "2024-01-15T18:30:00")
        @FutureOrPresent(message = "Start time must be in the future")
        @NotNull(message = "Start time is required")
        LocalDateTime startTime,

        @Schema(description = "Base price for a standard seat", example = "150.00")
        @DecimalMin(value = "10.0", message = "Base price must be at least 10")
        @NotNull(message = "Base price is required")
        BigDecimal basePrice,

        @Schema(description = "ID of the movie being shown", example = "1")
        @NotNull(message = "Movie is required")
        Long movieId,

        @Schema(description = "ID of the cinema hall", example = "2")
        @NotNull(message = "Hall is required")
        Long hallId
) {
}