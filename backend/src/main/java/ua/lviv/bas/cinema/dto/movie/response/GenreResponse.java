package ua.lviv.bas.cinema.dto.movie.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for movie genre information")
public class GenreResponse {

	@Schema(description = "Unique identifier of the genre", example = "1")
	private Long id;

	@Schema(description = "Name of the genre", example = "Action")
	private String name;
}