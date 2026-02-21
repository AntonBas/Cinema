package ua.lviv.bas.cinema.domain.projection;

public interface MovieSessionSearchProjection {
	Long getId();

	String getTitle();

	Integer getDurationMinutes();
}