package ua.lviv.bas.cinema.exception.core;

import java.io.Serial;

public class InvalidSortPropertyException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidSortPropertyException(String property) {
        super("Invalid sort property: " + property, "INVALID_SORT_PROPERTY");
    }
}
