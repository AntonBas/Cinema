package ua.lviv.bas.cinema.exception.domain.auth;

import ua.lviv.bas.cinema.exception.core.ValidationException;

import java.io.Serial;

public class TokenExpiredException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TokenExpiredException(String tokenType) {
        super(String.format("%s token has expired", tokenType), "TOKEN_EXPIRED",
                String.format("The %s token has exceeded its validity period", tokenType));
    }
}