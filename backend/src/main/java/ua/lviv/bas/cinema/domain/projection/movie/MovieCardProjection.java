package ua.lviv.bas.cinema.domain.projection.movie;

import java.time.LocalDate;

import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;

public interface MovieCardProjection {
	Long getId();

	String getSlug();

	String getTitle();

	String getPosterFileName();

	Integer getDurationMinutes();

	AgeRating getAgeRating();

	MovieStatus getStatus();

	LocalDate getReleaseDate();

	LocalDate getEndShowingDate();

	default String getPosterUrl() {
		return "/api/movies/" + getId() + "/poster";
	}
}