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

	default String getPosterUrl() {
		return "/api/movies/" + getId() + "/poster";
	}

	default boolean isCurrentlyShowing() {
		return getStatus() == MovieStatus.CURRENT;
	}

	default boolean isUpcoming() {
		return getStatus() == MovieStatus.UPCOMING;
	}

	default boolean isArchived() {
		return getStatus() == MovieStatus.ARCHIVED;
	}
}