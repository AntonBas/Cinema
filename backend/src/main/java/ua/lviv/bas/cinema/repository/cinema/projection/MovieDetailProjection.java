package ua.lviv.bas.cinema.repository.cinema.projection;

import java.time.LocalDate;

import ua.lviv.bas.cinema.domain.cinema.enums.AgeRating;
import ua.lviv.bas.cinema.domain.cinema.status.MovieStatus;

public interface MovieDetailProjection {
	Long getId();

	String getTitle();

	String getSlug();

	String getTrailerUrl();

	String getDescription();

	Integer getDurationMinutes();

	LocalDate getReleaseDate();

	LocalDate getEndShowingDate();

	AgeRating getAgeRating();

	MovieStatus getStatus();

	String getPosterFileName();

	default String getPosterUrl() {
		return "/api/movies/" + getId() + "/poster";
	}
}