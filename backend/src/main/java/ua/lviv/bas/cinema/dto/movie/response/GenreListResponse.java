package ua.lviv.bas.cinema.dto.movie.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Genre with movie count for list view")
public record GenreListResponse(
        @Schema(description = "Unique identifier of the genre", example = "1")
        Long id,

        @Schema(description = "Name of the genre", example = "Action")
        String name,

        @Schema(description = "Number of movies in this genre", example = "25")
        Integer movieCount
) {
}