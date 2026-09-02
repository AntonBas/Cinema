package ua.lviv.bas.cinema.exception.domain.booking;

import ua.lviv.bas.cinema.exception.core.ConflictException;

import java.io.Serial;

public class BookingConcurrentModificationException extends ConflictException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BookingConcurrentModificationException(Long bookingId) {
        super("Booking was modified concurrently, please retry", "BOOKING_CONCURRENT_MODIFICATION",
                "Optimistic lock conflict while saving booking " + bookingId);
    }
}
