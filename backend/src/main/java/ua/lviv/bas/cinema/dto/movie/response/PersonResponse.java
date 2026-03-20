package ua.lviv.bas.cinema.dto.movie.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.enums.PersonRole;

public record PersonResponse(@Schema(description = "Unique identifier of the person", example = "1") Long id,

		@Schema(description = "Full name of the person", example = "Leonardo DiCaprio") String name,

		@Schema(description = "Role of the person in movies", example = "ACTOR") PersonRole role,

		@Schema(description = "Number of movies this person appears in", example = "15") Integer movieCount) {
}