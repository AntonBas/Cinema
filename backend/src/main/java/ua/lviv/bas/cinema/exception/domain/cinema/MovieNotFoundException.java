package ua.lviv.bas.cinema.exception.domain.cinema;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

import java.io.Serial;

public class MovieNotFoundException extends NotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public MovieNotFoundException(Long movieId) {
        super("Movie not found", "MOVIE_NOT_FOUND",
                String.format("Movie entity with id %d does not exist", movieId));
    }

    public MovieNotFoundException(String slug) {
        super("Movie not found", "MOVIE_NOT_FOUND",
                String.format("Movie entity with slug '%s' does not exist", slug));
    }
}