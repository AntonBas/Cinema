package ua.lviv.bas.cinema.domain.projection;

import java.time.LocalDate;

import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;

public interface MovieCardProjection {
	Long getId();

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