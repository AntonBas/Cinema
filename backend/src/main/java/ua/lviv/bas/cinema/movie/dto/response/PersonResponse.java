package ua.lviv.bas.cinema.movie.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.movie.domain.enums.PersonRole;

@Schema(description = "Movie person details")
public record PersonResponse(

        @Schema(description = "Unique identifier of the person", example = "1")
        Long id,

        @Schema(description = "Full name of the person", example = "Leonardo DiCaprio")
        String name,

        @Schema(description = "Role of the person", example = "ACTOR")
        PersonRole role
) {
}