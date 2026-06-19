package ua.lviv.bas.cinema.exception.domain.booking;

import ua.lviv.bas.cinema.exception.core.ConflictException;

import java.io.Serial;

public class SeatNotAvailableException extends ConflictException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SeatNotAvailableException(String message) {
        super(message, "SEAT_NOT_AVAILABLE", message);
    }

    public static SeatNotAvailableException forSeatAndSession(Long seatId, Long sessionId) {
        return new SeatNotAvailableException(
                String.format("Seat %d is not available for session %d", seatId, sessionId));
    }

    public static SeatNotAvailableException seatInactive(Long seatId) {
        return new SeatNotAvailableException(String.format("Seat %d is not active", seatId));
    }
}