package ua.lviv.bas.cinema.movie.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ua.lviv.bas.cinema.movie.domain.enums.PersonRole;

@Schema(description = "Request to create or update a movie person")
public record PersonRequest(
        @Schema(description = "Full name of the person", example = "Leonardo DiCaprio", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Person name is required")
        @Size(max = 50, message = "Person name must not exceed 50 characters")
        String name,

        @Schema(description = "Role of the person in movies", example = "ACTOR", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Person role is required")
        PersonRole role
) {
}