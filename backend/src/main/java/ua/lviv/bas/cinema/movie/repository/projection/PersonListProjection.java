package ua.lviv.bas.cinema.movie.repository.projection;

import ua.lviv.bas.cinema.movie.domain.enums.PersonRole;

public interface PersonListProjection {
    Long getId();

    String getName();

    PersonRole getRole();

    Integer getMovieCount();
}