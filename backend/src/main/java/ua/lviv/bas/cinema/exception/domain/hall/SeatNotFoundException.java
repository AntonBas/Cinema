package ua.lviv.bas.cinema.exception.domain.hall;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

import java.io.Serial;

public class SeatNotFoundException extends NotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SeatNotFoundException(Long seatId) {
        super("Seat not found", "SEAT_NOT_FOUND",
                String.format("Seat entity with id %d does not exist", seatId));
    }
}