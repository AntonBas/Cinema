package ua.lviv.bas.cinema.movie.person;

import ua.lviv.bas.cinema.movie.person.PersonRole;

public interface PersonListProjection {
    Long getId();

    String getName();

    PersonRole getRole();

    Integer getMovieCount();
}