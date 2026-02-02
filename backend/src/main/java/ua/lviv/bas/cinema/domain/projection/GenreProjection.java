package ua.lviv.bas.cinema.domain.projection;

public interface GenreProjection {
	Long getId();

	String getName();

	Integer getMovieCount();
}