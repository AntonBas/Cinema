package ua.lviv.bas.cinema.exception.domain.cinema;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

public class PersonNotFoundException extends NotFoundException {

	private static final long serialVersionUID = 1L;

	public PersonNotFoundException(Long personId) {
		super(String.format("Person with id '%d' not found", personId), "PERSON_NOT_FOUND",
				String.format("Person entity with id %d does not exist in database", personId));
	}

	public PersonNotFoundException(String name) {
		super(String.format("Person with name '%s' not found", name), "PERSON_NOT_FOUND",
				String.format("Person entity with name %s does not exist in database", name));
	}
}