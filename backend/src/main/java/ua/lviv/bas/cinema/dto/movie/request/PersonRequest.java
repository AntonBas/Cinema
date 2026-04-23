package ua.lviv.bas.cinema.dto.movie.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ua.lviv.bas.cinema.domain.cinema.enums.PersonRole;

public record PersonRequest(
        @Schema(description = "Full name of the person", example = "Leonardo DiCaprio", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Person name is required")
        String name,

        @Schema(description = "Role of the person in movies", example = "ACTOR", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Person role is required")
        PersonRole role
) {
}