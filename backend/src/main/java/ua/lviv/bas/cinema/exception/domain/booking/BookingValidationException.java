package ua.lviv.bas.cinema.exception.domain.booking;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class BookingValidationException extends ValidationException {
	private static final long serialVersionUID = 1L;

	public BookingValidationException(String message) {
		super(message, "BOOKING_ERROR", message);
	}

	public static BookingValidationException sessionNotAvailable() {
		return new BookingValidationException("Session is not available for booking");
	}

	public static BookingValidationException sessionAlreadyStarted() {
		return new BookingValidationException("Session has already started");
	}

	public static BookingValidationException sessionTooClose() {
		return new BookingValidationException("Session starts in less than 30 minutes");
	}

	public static BookingValidationException cannotCancel() {
		return new BookingValidationException("Booking cannot be cancelled");
	}

	public static BookingValidationException bookingExpired() {
		return new BookingValidationException("Booking has expired. Please create a new booking.");
	}

	public static BookingValidationException tempHoldExpired() {
		return new BookingValidationException("Temporary hold has expired. Please select seats again.");
	}
}