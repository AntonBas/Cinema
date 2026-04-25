package ua.lviv.bas.cinema.exception.domain.auth;

import ua.lviv.bas.cinema.exception.core.ConflictException;

import java.io.Serial;

public class EmailAlreadyExistsException extends ConflictException {

    @Serial
    private static final long serialVersionUID = 1L;

    public EmailAlreadyExistsException(String email) {
        super(String.format("Email '%s' is already registered", email), "EMAIL_ALREADY_EXISTS",
                String.format("User with email %s already exists in database", email));
    }
}