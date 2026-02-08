package ua.lviv.bas.cinema.domain.projection;

import java.time.LocalDate;

public interface MovieSessionSearchProjection {
	Long getId();

	String getTitle();

	LocalDate getReleaseDate();

	Integer getDurationMinutes();
}