package ua.lviv.bas.cinema.exception.domain.cinema;

import java.time.LocalDate;
import java.time.LocalDateTime;

import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.exception.core.ValidationException;

public class SessionValidationException extends ValidationException {

	private static final long serialVersionUID = 1L;

	public SessionValidationException(String message, String errorCode) {
		super(message, errorCode);
	}

	public static SessionValidationException tooCloseToStart(LocalDateTime startTime) {
		return new SessionValidationException(
				String.format("Session must start at least 30 minutes from now. Start time: %s", startTime),
				"SESSION_TOO_CLOSE");
	}

	public static SessionValidationException movieNotReleased(Movie movie, LocalDate sessionDate) {
		return new SessionValidationException(String.format("Movie '%s' releases on %s - cannot create session for %s",
				movie.getTitle(), movie.getReleaseDate(), sessionDate), "MOVIE_NOT_RELEASED");
	}

	public static SessionValidationException movieEndedShowing(Movie movie, LocalDate sessionDate) {
		return new SessionValidationException(
				String.format("Movie '%s' ended showing on %s - cannot create session for %s", movie.getTitle(),
						movie.getEndShowingDate(), sessionDate),
				"MOVIE_ENDED_SHOWING");
	}
}