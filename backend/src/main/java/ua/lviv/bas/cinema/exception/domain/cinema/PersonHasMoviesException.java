package ua.lviv.bas.cinema.exception.domain.cinema;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

public class PersonHasMoviesException extends BusinessException {

	private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "PERSON_HAS_MOVIES";
	private static final HttpStatus STATUS = HttpStatus.CONFLICT;

	public PersonHasMoviesException(Long personId, String personName, long totalMovieCount) {
		super(String.format("Person '%s' (id: %d) cannot be deleted", personName, personId), ERROR_CODE, STATUS,
				String.format("Person '%s' is associated with %d movie(s) and cannot be deleted", personName,
						totalMovieCount));
	}
}