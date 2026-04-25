package ua.lviv.bas.cinema.exception.domain.user;

import ua.lviv.bas.cinema.exception.core.ConflictException;

import java.io.Serial;

public class LastAdminException extends ConflictException {

    @Serial
    private static final long serialVersionUID = 1L;

    public LastAdminException() {
        super("Cannot remove the last administrator", "LAST_ADMIN", "System must have at least one administrator user");
    }
}