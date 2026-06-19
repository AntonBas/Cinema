package ua.lviv.bas.cinema.dto.session.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Session information for public schedule view")
public record SessionScheduleResponse(
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

        @Schema(description = "ID of the movie", example = "5")
        Long movieId,

        @Schema(description = "Title of the movie", example = "Inception")
        String movieTitle,

        @Schema(description = "Movie poster file name", example = "inception.jpg")
        String moviePosterFileName,

        @Schema(description = "Age rating", example = "PG-13")
        String movieAgeRating,

        @Schema(description = "Duration in minutes", example = "148")
        Integer movieDuration,

        @Schema(description = "ID of the cinema hall", example = "3")
        Long hallId,

        @Schema(description = "Name of the cinema hall", example = "Hall A")
        String hallName,

        @Schema(description = "Hall capacity", example = "150")
        Integer hallCapacity
) {
    public SessionScheduleResponse withAvailableSeats(Integer availableSeats) {
        return new SessionScheduleResponse(
                id, startTime, endTime, basePrice, availableSeats, movieId, movieTitle,
                moviePosterFileName, movieAgeRating, movieDuration, hallId, hallName, hallCapacity
        );
    }
}