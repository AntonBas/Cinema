package ua.lviv.bas.cinema.exception.domain.auth;

import ua.lviv.bas.cinema.exception.core.ValidationException;

import java.io.Serial;

public class EmailAlreadyVerifiedException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public EmailAlreadyVerifiedException() {
        super("Email is already verified", "EMAIL_ALREADY_VERIFIED");
    }
}
