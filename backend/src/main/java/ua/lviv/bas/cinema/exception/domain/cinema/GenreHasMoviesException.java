package ua.lviv.bas.cinema.exception.domain.cinema;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

import java.io.Serial;

public class GenreHasMoviesException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public GenreHasMoviesException(Long genreId, String genreName, long totalMovieCount) {
        super(
                String.format("Genre '%s' cannot be deleted", genreName),
                "GENRE_HAS_MOVIES",
                HttpStatus.CONFLICT,
                String.format("Genre '%s' (id: %d) is associated with %d movie(s) and cannot be deleted",
                        genreName, genreId, totalMovieCount)
        );
    }
}