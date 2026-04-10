package ua.lviv.bas.cinema.exception.domain.hall;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

public class SeatNotFoundException extends NotFoundException {

	private static final long serialVersionUID = 1L;

	public SeatNotFoundException(Long seatId) {
		super(String.format("Seat with id '%d' not found", seatId), "SEAT_NOT_FOUND",
				String.format("Seat entity with id %d does not exist", seatId));
	}

	public SeatNotFoundException(Long hallId, Integer row, Integer number) {
		super(String.format("Seat at row %d, number %d in hall %d not found", row, number, hallId), "SEAT_NOT_FOUND",
				String.format("Seat at position [%d,%d] in hall %d does not exist", row, number, hallId));
	}
}