package ua.lviv.bas.cinema.repository.cinema.projection;

import java.time.LocalDate;

import ua.lviv.bas.cinema.domain.cinema.enums.AgeRating;
import ua.lviv.bas.cinema.domain.cinema.status.MovieStatus;

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