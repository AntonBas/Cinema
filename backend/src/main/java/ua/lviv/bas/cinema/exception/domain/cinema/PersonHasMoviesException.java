package ua.lviv.bas.cinema.exception.domain.cinema;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

import java.io.Serial;

public class PersonHasMoviesException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PersonHasMoviesException(String personName, long totalMovieCount) {
        super(
                String.format("Person '%s' cannot be deleted", personName),
                "PERSON_HAS_MOVIES",
                HttpStatus.CONFLICT,
                String.format("Person '%s' is associated with %d movie(s) and cannot be deleted",
                        personName, totalMovieCount)
        );
    }
}