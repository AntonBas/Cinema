package ua.lviv.bas.cinema.exception.domain.hall;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

import java.io.Serial;

public class CinemaHallHasSessionsException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CinemaHallHasSessionsException(String hallName, Long hallId) {
        this(String.format("'%s' has scheduled sessions and cannot be modified", hallName),
                String.format("Hall '%s' (id: %d) has future sessions", hallName, hallId));
    }

    private CinemaHallHasSessionsException(String message, String debugMessage) {
        super(message, "CINEMA_HALL_HAS_SESSIONS", HttpStatus.CONFLICT, debugMessage);
    }

    public static CinemaHallHasSessionsException cannotDelete(String hallName, Long hallId) {
        return new CinemaHallHasSessionsException(
                String.format("'%s' has session history and cannot be deleted", hallName),
                String.format("Hall '%s' (id: %d) is referenced by sessions", hallName, hallId));
    }
}