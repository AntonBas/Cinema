package ua.lviv.bas.cinema.dto.movie.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating or updating a movie genre")
public class GenreRequest {

	@Schema(description = "Name of the genre", example = "Action", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 30)
	@NotBlank(message = "Genre name is required")
	@Size(max = 30, min = 2, message = "Name must be between 2 and 30 characters")
	private String name;
}