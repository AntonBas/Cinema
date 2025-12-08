package ua.lviv.bas.cinema.dto.movie.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response DTO for detailed movie information")
public class MovieDetailResponse {

	@Schema(description = "Unique identifier of the movie", example = "1")
	private Long id;

	@Schema(description = "Title of the movie", example = "Inception")
	private String title;

	@Schema(description = "URL-friendly slug for the movie", example = "inception-2020")
	private String slug;

	@Schema(description = "URL to the movie trailer", example = "https://www.youtube.com/watch?v=YoHD9XEInc0")
	private String trailerUrl;

	@Schema(description = "Movie description/synopsis", example = "A thief who steals corporate secrets through dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O.")
	private String description;

	@Schema(description = "Duration of the movie in minutes", example = "148")
	private Integer durationMinutes;

	@Schema(description = "Release date of the movie", example = "2024-01-15", type = "string", format = "date")
	private LocalDate releaseDate;

	@Schema(description = "Date when the movie stops showing in cinemas", example = "2024-03-15", type = "string", format = "date")
	private LocalDate endShowingDate;

	@Schema(description = "Age rating of the movie", example = "PG_13", allowableValues = { "G", "PG", "PG_13", "R",
			"NC_17" })
	private AgeRating ageRating;

	@Schema(description = "Current status of the movie", example = "ACTIVE", allowableValues = { "ACTIVE", "UPCOMING",
			"ARCHIVED", "CANCELLED" })
	private MovieStatus status;

	@Schema(description = "File name of the movie poster", example = "inception-poster.jpg")
	private String posterFileName;

	@Schema(description = "URL to the movie poster image", example = "/api/movies/1/poster")
	private String posterUrl;

	@Schema(description = "Indicates if the movie is currently showing in cinemas", example = "true")
	private boolean currentlyShowing;

	@Schema(description = "Indicates if the movie is upcoming (not yet released)", example = "false")
	private boolean upcoming;

	@Schema(description = "Indicates if the movie is archived", example = "false")
	private boolean archived;

	@Schema(description = "Indicates if the movie is active", example = "true")
	private boolean active;

	@Schema(description = "List of genres associated with the movie")
	private List<GenreResponse> genres;

	@Schema(description = "List of actors in the movie")
	private List<PersonResponse> actors;

	@Schema(description = "List of directors of the movie")
	private List<PersonResponse> directors;

	@Schema(description = "List of screenwriters of the movie")
	private List<PersonResponse> screenwriters;
}