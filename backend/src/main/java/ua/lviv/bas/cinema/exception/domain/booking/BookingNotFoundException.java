package ua.lviv.bas.cinema.exception.domain.booking;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

public class BookingNotFoundException extends NotFoundException {
	private static final long serialVersionUID = 1L;

	public BookingNotFoundException(Long id) {
		super("Booking not found", "BOOKING_NOT_FOUND", String.format("Booking with ID %d does not exist", id));
	}
}