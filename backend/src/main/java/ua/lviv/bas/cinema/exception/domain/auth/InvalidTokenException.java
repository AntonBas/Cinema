package ua.lviv.bas.cinema.exception.domain.auth;

import ua.lviv.bas.cinema.exception.core.ValidationException;

import java.io.Serial;

public class InvalidTokenException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidTokenException(String tokenType) {
        super(String.format("Invalid %s token", tokenType), "INVALID_TOKEN",
                String.format("The provided %s token is malformed", tokenType));
    }
}