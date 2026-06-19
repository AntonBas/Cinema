package ua.lviv.bas.cinema.exception.domain.cinema;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

import java.io.Serial;

public class PersonNotFoundException extends NotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PersonNotFoundException(Long personId) {
        super("Person not found", "PERSON_NOT_FOUND",
                String.format("Person entity with id %d does not exist", personId));
    }
}