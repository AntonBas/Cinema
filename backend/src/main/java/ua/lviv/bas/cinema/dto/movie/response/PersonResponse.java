package ua.lviv.bas.cinema.dto.movie.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.PersonRole;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for person information (actor, director, screenwriter)")
public class PersonResponse {

	@Schema(description = "Unique identifier of the person", example = "1")
	private Long id;

	@Schema(description = "Full name of the person", example = "Leonardo DiCaprio")
	private String name;

	@Schema(description = "Role of the person in movies", example = "ACTOR", allowableValues = { "ACTOR", "DIRECTOR",
			"SCREENWRITER" })
	private PersonRole role;
}