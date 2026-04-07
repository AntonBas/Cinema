package ua.lviv.bas.cinema.dto.movie.response;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.cinema.enums.AgeRating;
import ua.lviv.bas.cinema.domain.cinema.status.MovieStatus;
import ua.lviv.bas.cinema.dto.session.response.MovieSessionInfoResponse;

public record MovieDetailResponse(@Schema(description = "Unique identifier of the movie", example = "1") Long id,

		@Schema(description = "Title of the movie", example = "Inception") String title,

		@Schema(description = "URL-friendly slug for the movie", example = "inception-2020") String slug,

		@Schema(description = "URL to the movie trailer", example = "https://www.youtube.com/watch?v=YoHD9XEInc0") String trailerUrl,

		@Schema(description = "Movie description/synopsis") String description,

		@Schema(description = "Duration of the movie in minutes", example = "148") Integer durationMinutes,

		@Schema(description = "Release date of the movie", example = "2024-01-15") LocalDate releaseDate,

		@Schema(description = "Date when the movie stops showing in cinemas", example = "2024-03-15") LocalDate endShowingDate,

		@Schema(description = "Age rating of the movie", example = "PG_13") AgeRating ageRating,

		@Schema(description = "Current status of the movie", example = "ACTIVE") MovieStatus status,

		@Schema(description = "File name of the movie poster", example = "inception-poster.jpg") String posterFileName,

		@Schema(description = "URL to the movie poster image", example = "/api/movies/1/poster") String posterUrl,

		@Schema(description = "List of genres associated with the movie") List<GenreInfoResponse> genres,

		@Schema(description = "List of actors in the movie") List<PersonInfoResponse> actors,

		@Schema(description = "List of directors of the movie") List<PersonInfoResponse> directors,

		@Schema(description = "List of screenwriters of the movie") List<PersonInfoResponse> screenwriters,

		@Schema(description = "List of sessions for this movie") List<MovieSessionInfoResponse> sessions) {
}