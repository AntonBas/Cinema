package ua.lviv.bas.cinema.domain.projection;

import java.time.LocalDate;

import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;

public interface MovieProjection {
	Long getId();

	String getTitle();

	String getSlug();

	String getTrailerUrl();

	String getDescription();

	Integer getDurationMinutes();

	LocalDate getReleaseDate();

	LocalDate getEndShowingDate();

	MovieStatus getStatus();

	String getPosterFileName();

	AgeRating getAgeRating();

	String getGenreNames();

	String getActorNames();

	String getDirectorNames();

	String getScreenwriterNames();
}