package ua.lviv.bas.cinema.exception.domain.hall;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

import java.io.Serial;

public class CinemaHallHasSessionsException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CinemaHallHasSessionsException(String hallName, Long hallId) {
        super(String.format("'%s' has scheduled sessions and cannot be modified", hallName),
                "CINEMA_HALL_HAS_SESSIONS", HttpStatus.CONFLICT,
                String.format("Hall '%s' (id: %d) has future sessions", hallName, hallId));
    }
}