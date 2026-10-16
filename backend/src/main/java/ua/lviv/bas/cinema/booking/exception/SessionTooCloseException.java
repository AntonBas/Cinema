package ua.lviv.bas.cinema.booking.exception;

import ua.lviv.bas.cinema.common.DateTimeFormatterService;
import ua.lviv.bas.cinema.exception.core.ValidationException;

import java.io.Serial;
import java.time.LocalDateTime;

public class SessionTooCloseException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SessionTooCloseException(LocalDateTime sessionTime) {
        super(String.format("Cannot pay for session starting at %s (less than 30 minutes remaining)",
                        sessionTime.format(DateTimeFormatterService.STANDARD_FORMATTER)), "SESSION_TOO_CLOSE",
                String.format("Session time: %s", sessionTime));
    }
}