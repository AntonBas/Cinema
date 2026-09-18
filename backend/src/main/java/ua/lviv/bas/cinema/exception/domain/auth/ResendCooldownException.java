package ua.lviv.bas.cinema.exception.domain.auth;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import ua.lviv.bas.cinema.exception.core.BusinessException;

import java.io.Serial;

@Getter
public class ResendCooldownException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final long remainingSeconds;

    public ResendCooldownException(long remainingSeconds) {
        super("Please wait before requesting another verification email", "RESEND_COOLDOWN_ACTIVE",
                HttpStatus.TOO_MANY_REQUESTS, String.format("%d seconds remaining before resend is allowed", remainingSeconds));
        this.remainingSeconds = remainingSeconds;
    }
}
