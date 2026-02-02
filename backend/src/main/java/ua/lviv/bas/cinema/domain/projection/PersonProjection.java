package ua.lviv.bas.cinema.domain.projection;

import ua.lviv.bas.cinema.domain.enums.PersonRole;

public interface PersonProjection {
	Long getId();

	String getName();

	PersonRole getRole();

	Integer getMovieCount();
}