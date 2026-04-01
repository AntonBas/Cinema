package ua.lviv.bas.cinema.repository.cinema.projection;

import ua.lviv.bas.cinema.domain.cinema.enums.PersonRole;

public interface PersonProjection {
	Long getId();

	String getName();

	PersonRole getRole();

	Integer getMovieCount();
}