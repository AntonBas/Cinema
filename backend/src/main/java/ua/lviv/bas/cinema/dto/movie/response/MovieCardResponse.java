package ua.lviv.bas.cinema.dto.movie.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.cinema.enums.AgeRating;
import ua.lviv.bas.cinema.domain.cinema.status.MovieStatus;

public record MovieCardResponse(

        @Schema(description = "Unique identifier of the movie", example = "1")
        Long id,

        @Schema(description = "URL-friendly slug for the movie", example = "inception")
        String slug,

        @Schema(description = "Title of the movie", example = "Inception")
        String title,

        @Schema(description = "URL to the movie poster image", example = "/api/movies/1/poster")
        String posterUrl,

        @Schema(description = "Duration of the movie in minutes", example = "148")
        Integer durationMinutes,

        @Schema(description = "Age rating of the movie", example = "PEGI_12")
        AgeRating ageRating,

        @Schema(description = "Current status of the movie", example = "CURRENT")
        MovieStatus status
) {
}