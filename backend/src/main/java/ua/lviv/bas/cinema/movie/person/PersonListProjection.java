package ua.lviv.bas.cinema.movie.person;


public interface PersonListProjection {
    Long getId();

    String getName();

    PersonRole getRole();

    Integer getMovieCount();
}