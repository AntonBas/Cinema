package ua.lviv.bas.cinema.dto.movie.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MovieSessionSearchResponse(

        @Schema(description = "Unique identifier of the movie", example = "1")
        Long id,

        @Schema(description = "Title of the movie", example = "Inception")
        String title,

        @Schema(description = "Duration of the movie in minutes", example = "148")
        Integer durationMinutes
) {
}