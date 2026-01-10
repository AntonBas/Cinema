package ua.lviv.bas.cinema.exception.domain.booking;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class BookingOperationException extends ValidationException {
	private static final long serialVersionUID = 1L;

	public BookingOperationException(String message) {
		super(message, "INVALID_BOOKING_OPERATION");
	}

	public static BookingOperationException onlyPendingCanBeConfirmed() {
		return new BookingOperationException("Only pending bookings can be confirmed");
	}

	public static BookingOperationException cannotExpireNonPending() {
		return new BookingOperationException("Cannot expire a booking that is not in PENDING status");
	}
}