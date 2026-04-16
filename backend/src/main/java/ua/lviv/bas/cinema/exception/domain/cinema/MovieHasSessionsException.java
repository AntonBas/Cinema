package ua.lviv.bas.cinema.exception.domain.cinema;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

public class MovieHasSessionsException extends BusinessException {

	private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "MOVIE_HAS_SESSIONS";
	private static final HttpStatus STATUS = HttpStatus.CONFLICT;

	public MovieHasSessionsException(Long movieId, String movieTitle, long totalSessionCount) {
		super(String.format("Movie '%s' (id: %d) cannot be deleted", movieTitle, movieId), ERROR_CODE, STATUS,
				String.format("Movie '%s' is associated with %d session(s) and cannot be deleted", movieTitle,
						totalSessionCount));
	}
}