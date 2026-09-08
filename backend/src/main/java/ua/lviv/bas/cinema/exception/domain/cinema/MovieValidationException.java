package ua.lviv.bas.cinema.exception.domain.cinema;

import ua.lviv.bas.cinema.exception.core.ValidationException;

import java.io.Serial;
import java.time.LocalDate;

public class MovieValidationException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public MovieValidationException(String message, String errorCode) {
        super(message, errorCode);
    }

    public static MovieValidationException endShowingBeforeRelease(LocalDate releaseDate, LocalDate endShowingDate) {
        return new MovieValidationException(
                String.format("End showing date %s must be after release date %s", endShowingDate, releaseDate),
                "END_SHOWING_BEFORE_RELEASE");
    }
}
