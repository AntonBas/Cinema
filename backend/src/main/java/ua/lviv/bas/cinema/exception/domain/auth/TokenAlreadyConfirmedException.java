package ua.lviv.bas.cinema.exception.domain.auth;

import ua.lviv.bas.cinema.exception.core.ConflictException;

import java.io.Serial;

public class TokenAlreadyConfirmedException extends ConflictException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TokenAlreadyConfirmedException(String tokenType) {
        super(String.format("%s token has already been confirmed", tokenType), "TOKEN_ALREADY_CONFIRMED",
                String.format("The %s token was already used", tokenType));
    }
}