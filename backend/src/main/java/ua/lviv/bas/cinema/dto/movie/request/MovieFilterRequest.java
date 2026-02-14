package ua.lviv.bas.cinema.dto.movie.request;

import java.time.LocalDate;

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
@Schema(description = "Request DTO for filtering and searching movies")
public class MovieFilterRequest {

	@Schema(description = "Search term for movie title", example = "Inception")
	private String title;

	@Schema(description = "Filter by movie status", example = "CURRENT")
	private MovieStatus status;

	@Schema(description = "Filter by age rating", example = "PG_13")
	private AgeRating ageRating;

	@Schema(description = "Filter currently showing movies", example = "true")
	private Boolean currentlyShowing;

	@Schema(description = "Filter upcoming movies", example = "true")
	private Boolean upcoming;

	@Schema(description = "Filter archived movies", example = "true")
	private Boolean archived;

	@Schema(description = "Filter by release date from", example = "2024-01-01")
	private LocalDate releaseDateFrom;

	@Schema(description = "Filter by release date to", example = "2024-12-31")
	private LocalDate releaseDateTo;

	@Schema(description = "Filter by genre ID", example = "1")
	private Long genreId;

	@Schema(description = "Filter by actor ID", example = "5")
	private Long actorId;

	@Schema(description = "Filter by director ID", example = "8")
	private Long directorId;

	@Schema(description = "Filter by screenwriter ID", example = "10")
	private Long screenwriterId;
}