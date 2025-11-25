package ua.lviv.bas.cinema.exception.domain.cinema;

import ua.lviv.bas.cinema.exception.core.ConflictException;

public class PersonHasMoviesException extends ConflictException {

	private static final long serialVersionUID = 1L;

	public PersonHasMoviesException(Long personId) {
		super(String.format("Cannot delete person with id '%d' because they have associated movies", personId),
				"PERSON_HAS_MOVIES",
				String.format("Person entity with id %d has movie associations that prevent deletion", personId));
	}

	public PersonHasMoviesException(String personName) {
		super(String.format("Cannot delete person '%s' because they have associated movies", personName),
				"PERSON_HAS_MOVIES", String.format("Person %s has movie associations", personName));
	}
}