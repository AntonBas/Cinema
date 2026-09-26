package ua.lviv.bas.cinema.cinema.dto.hall.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create or rename a cinema hall")
public record CinemaHallRequest(
        @Schema(description = "Name of the cinema hall", example = "Hall A - Dolby Atmos", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 25)
        @NotBlank(message = "Hall name is required")
        @Size(min = 2, max = 25, message = "Name must be between 2-25 characters")
        String name
) {
}
