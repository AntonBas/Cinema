package ua.lviv.bas.cinema.domain.projection;

import ua.lviv.bas.cinema.domain.enums.PersonRole;

public interface PersonProjection {
	Long getId();

	String getName();

	PersonRole getRole();

	Integer getMovieCount();

	default boolean isPopular() {
		return getMovieCount() != null && getMovieCount() > 5;
	}
}