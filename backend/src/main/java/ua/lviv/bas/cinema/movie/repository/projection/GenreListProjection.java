package ua.lviv.bas.cinema.movie.repository.projection;

public interface GenreListProjection {
    Long getId();

    String getName();

    Integer getMovieCount();
}