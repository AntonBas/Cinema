package ua.lviv.bas.cinema.exception.domain.hall;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

import java.io.Serial;

public class CinemaHallNotFoundException extends NotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CinemaHallNotFoundException(Long hallId) {
        super("Cinema hall not found", "CINEMA_HALL_NOT_FOUND",
                String.format("Cinema hall entity with id %d does not exist", hallId));
    }
}