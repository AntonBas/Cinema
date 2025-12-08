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
@Schema(description = "Response DTO for movie search in session management")
public class MovieSessionSearchResponse {

	@Schema(description = "Unique identifier of the movie", example = "1")
	private Long id;

	@Schema(description = "Title of the movie", example = "Inception")
	private String title;

	@Schema(description = "Release year of the movie", example = "2024")
	private Integer releaseYear;

	@Schema(description = "Duration of the movie in minutes", example = "148")
	private Integer durationMinutes;
}