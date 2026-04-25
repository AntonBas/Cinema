package ua.lviv.bas.cinema.exception.domain.cinema;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

import java.io.Serial;

public class GenreNotFoundException extends NotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public GenreNotFoundException(Long genreId) {
        super("Genre not found", "GENRE_NOT_FOUND",
                String.format("Genre entity with id %d does not exist", genreId));
    }
}