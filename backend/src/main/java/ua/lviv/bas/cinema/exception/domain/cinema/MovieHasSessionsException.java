package ua.lviv.bas.cinema.exception.domain.cinema;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

import java.io.Serial;

public class MovieHasSessionsException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public MovieHasSessionsException(String movieTitle, long totalSessionCount) {
        super(
                String.format("Movie '%s' cannot be deleted", movieTitle),
                "MOVIE_HAS_SESSIONS",
                HttpStatus.CONFLICT,
                String.format("Movie '%s' is associated with %d session(s) and cannot be deleted",
                        movieTitle, totalSessionCount)
        );
    }
}