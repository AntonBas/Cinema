package ua.lviv.bas.cinema.exception.domain.user;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

import java.io.Serial;

public class EmailNotVerifiedException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public EmailNotVerifiedException(String action) {
        super(String.format("Cannot %s: email is not verified", action), "EMAIL_NOT_VERIFIED",
                HttpStatus.BAD_REQUEST,
                String.format("Email verification required for %s action", action));
    }
}