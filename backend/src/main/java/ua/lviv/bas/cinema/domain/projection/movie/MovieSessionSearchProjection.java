package ua.lviv.bas.cinema.domain.projection.movie;

public interface MovieSessionSearchProjection {
	Long getId();

	String getTitle();

	Integer getDurationMinutes();
}