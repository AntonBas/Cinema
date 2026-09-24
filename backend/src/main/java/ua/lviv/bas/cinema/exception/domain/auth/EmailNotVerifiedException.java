package ua.lviv.bas.cinema.exception.domain.auth;

import org.springframework.http.HttpStatus;
import ua.lviv.bas.cinema.exception.core.BusinessException;

import java.io.Serial;

public class EmailNotVerifiedException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public EmailNotVerifiedException() {
        super("Please confirm your email address before signing in", "EMAIL_NOT_VERIFIED", HttpStatus.FORBIDDEN,
                null);
    }
}
