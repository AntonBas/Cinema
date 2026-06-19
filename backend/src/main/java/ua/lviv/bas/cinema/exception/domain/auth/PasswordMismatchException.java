package ua.lviv.bas.cinema.exception.domain.auth;

import ua.lviv.bas.cinema.exception.core.ValidationException;

import java.io.Serial;

public class PasswordMismatchException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PasswordMismatchException() {
        super("Passwords do not match", "PASSWORD_MISMATCH",
                "The provided password and confirmation password do not match");
    }
}