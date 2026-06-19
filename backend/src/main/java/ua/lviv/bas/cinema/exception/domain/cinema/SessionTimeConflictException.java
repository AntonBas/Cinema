package ua.lviv.bas.cinema.exception.domain.cinema;

import ua.lviv.bas.cinema.exception.core.ConflictException;

import java.io.Serial;
import java.time.LocalDateTime;

public class SessionTimeConflictException extends ConflictException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SessionTimeConflictException(String hallName, LocalDateTime startTime) {
        super(String.format("Time conflict in hall '%s' at %s", hallName, startTime), "SESSION_TIME_CONFLICT",
                String.format("Hall '%s' already has a session at %s", hallName, startTime));
    }
}