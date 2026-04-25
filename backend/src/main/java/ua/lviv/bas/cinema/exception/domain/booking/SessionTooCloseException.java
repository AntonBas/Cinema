package ua.lviv.bas.cinema.exception.domain.booking;

import ua.lviv.bas.cinema.exception.core.ValidationException;

import java.io.Serial;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SessionTooCloseException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SessionTooCloseException(LocalDateTime sessionTime) {
        super(String.format("Cannot pay for session starting at %s (less than 30 minutes remaining)",
                        sessionTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))), "SESSION_TOO_CLOSE",
                String.format("Session time: %s", sessionTime));
    }
}