package ua.lviv.bas.cinema.dto.movie.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create or update a genre")
public record GenreRequest(
        @Schema(description = "Name of the genre", example = "Action", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 30)
        @NotBlank(message = "Genre name is required")
        @Size(max = 30, min = 2, message = "Name must be between 2 and 30 characters")
        String name
) {
}