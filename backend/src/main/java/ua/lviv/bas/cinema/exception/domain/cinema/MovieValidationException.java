package ua.lviv.bas.cinema.exception.domain.cinema;

import ua.lviv.bas.cinema.exception.core.ValidationException;
import ua.lviv.bas.cinema.movie.domain.enums.PersonRole;

import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    public static MovieValidationException durationChangeWithSessions() {
        return new MovieValidationException("Cannot change the duration of a movie that has scheduled sessions",
                "DURATION_CHANGE_WITH_SESSIONS");
    }

    public static MovieValidationException sessionsOutsideShowingPeriod(LocalDateTime sessionStart) {
        return new MovieValidationException(
                String.format("A scheduled session at %s falls outside the new showing period", sessionStart),
                "SESSIONS_OUTSIDE_SHOWING_PERIOD");
    }

    public static MovieValidationException posterRequired() {
        return new MovieValidationException("A movie must have a poster, upload a new one to replace it",
                "POSTER_REQUIRED");
    }

    public static MovieValidationException personRoleMismatch(String personName, PersonRole expectedRole) {
        return new MovieValidationException(
                String.format("'%s' cannot be added as %s", personName, expectedRole), "PERSON_ROLE_MISMATCH");
    }
}
