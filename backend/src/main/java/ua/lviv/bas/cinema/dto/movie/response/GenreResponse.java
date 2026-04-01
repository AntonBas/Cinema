package ua.lviv.bas.cinema.dto.movie.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record GenreResponse(@Schema(description = "Unique identifier of the genre", example = "1") Long id,
		@Schema(description = "Name of the genre", example = "Action") String name,
		@Schema(description = "Number of movies in this genre", example = "25") Integer movieCount) {
}