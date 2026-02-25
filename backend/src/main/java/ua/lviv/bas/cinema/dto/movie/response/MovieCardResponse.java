package ua.lviv.bas.cinema.dto.movie.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for movie card information (used in listings)")
public class MovieCardResponse {

	@Schema(description = "Unique identifier of the movie", example = "1")
	private Long id;

	@Schema(description = "URL-friendly slug for the movie", example = "inception")
	private String slug;

	@Schema(description = "Title of the movie", example = "Inception")
	private String title;

	@Schema(description = "URL to the movie poster image", example = "/api/movies/1/poster")
	private String posterUrl;

	@Schema(description = "Duration of the movie in minutes", example = "148")
	private Integer durationMinutes;

	@Schema(description = "Age rating of the movie", example = "PG_13")
	private AgeRating ageRating;

	@Schema(description = "Current status of the movie", example = "ACTIVE")
	private MovieStatus status;
}