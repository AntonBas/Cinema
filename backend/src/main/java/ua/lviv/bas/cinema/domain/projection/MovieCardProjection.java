package ua.lviv.bas.cinema.domain.projection;

import java.time.LocalDate;

import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;

public interface MovieCardProjection {
	Long getId();

	String getTitle();

	String getSlug();

	String getPosterFileName();

	Integer getDurationMinutes();

	AgeRating getAgeRating();

	MovieStatus getStatus();

	LocalDate getReleaseDate();

	LocalDate getEndShowingDate();
}