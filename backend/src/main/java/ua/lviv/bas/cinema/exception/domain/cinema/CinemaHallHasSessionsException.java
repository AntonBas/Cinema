package ua.lviv.bas.cinema.exception.domain.cinema;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

public class CinemaHallHasSessionsException extends BusinessException {

	private static final long serialVersionUID = 1L;

	public CinemaHallHasSessionsException(Long hallId) {
		super(String.format("Cinema hall with id '%d' has scheduled sessions and cannot be modified", hallId),
				"CINEMA_HALL_HAS_SESSIONS", HttpStatus.CONFLICT,
				String.format("Hall id %d has future sessions", hallId));
	}
}