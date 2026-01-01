package ua.lviv.bas.cinema.exception.domain.cinema;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

public class PersonHasMoviesException extends BusinessException {

	private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "PERSON_HAS_MOVIES";
	private static final HttpStatus STATUS = HttpStatus.CONFLICT;

	public PersonHasMoviesException(Long personId) {
		super(String.format("Person with id '%d' cannot be deleted", personId), ERROR_CODE, STATUS,
				String.format("Person entity with id %d is associated with movies and cannot be deleted", personId));
	}

	public PersonHasMoviesException(Long personId, String personName) {
		super(String.format("Person '%s' (id: %d) cannot be deleted", personName, personId), ERROR_CODE, STATUS,
				String.format("Person '%s' is associated with movies and cannot be deleted", personName));
	}

	public PersonHasMoviesException(Long personId, String personName, long actorCount, long directorCount,
			long screenwriterCount) {
		super(String.format("Person '%s' (id: %d) cannot be deleted", personName, personId), ERROR_CODE, STATUS,
				String.format(
						"Person '%s' is associated with %d movie(s): "
								+ "actor in %d, director in %d, screenwriter in %d",
						personName, actorCount + directorCount + screenwriterCount, actorCount, directorCount,
						screenwriterCount));
	}
}