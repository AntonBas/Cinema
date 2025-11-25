package ua.lviv.bas.cinema.exception.domain.cinema;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

public class CinemaHallNotFoundException extends NotFoundException {

	private static final long serialVersionUID = 1L;

	public CinemaHallNotFoundException(Long hallId) {
		super(String.format("Cinema hall with id '%d' not found", hallId), "CINEMA_HALL_NOT_FOUND",
				String.format("Cinema hall entity with id %d does not exist", hallId));
	}

	public CinemaHallNotFoundException(String hallName) {
		super(String.format("Cinema hall with name '%s' not found", hallName), "CINEMA_HALL_NOT_FOUND",
				String.format("Cinema hall entity with name %s does not exist", hallName));
	}
}