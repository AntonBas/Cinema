package ua.lviv.bas.cinema.domain.projection;

import java.time.LocalDate;

import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;

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
}