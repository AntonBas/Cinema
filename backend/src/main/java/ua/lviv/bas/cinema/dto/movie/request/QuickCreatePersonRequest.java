package ua.lviv.bas.cinema.dto.movie.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.PersonRole;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for quick creation of a person (used when creating movies)")
public class QuickCreatePersonRequest {

	@Schema(description = "Full name of the person", example = "Leonardo DiCaprio", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Person name is required")
	private String name;

	@Schema(description = "Role of the person in movies", example = "ACTOR", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {
			"ACTOR", "DIRECTOR", "SCREENWRITER" })
	@NotNull(message = "Person role is required")
	private PersonRole role;
}