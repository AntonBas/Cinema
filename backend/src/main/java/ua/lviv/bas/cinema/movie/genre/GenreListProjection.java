package ua.lviv.bas.cinema.movie.genre;

public interface GenreListProjection {
    Long getId();

    String getName();

    Integer getMovieCount();
}