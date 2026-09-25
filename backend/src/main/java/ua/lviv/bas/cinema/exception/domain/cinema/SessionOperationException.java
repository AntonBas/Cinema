package ua.lviv.bas.cinema.exception.domain.cinema;

import ua.lviv.bas.cinema.exception.core.ValidationException;

import java.io.Serial;

public class SessionOperationException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SessionOperationException(String message, String errorCode) {
        super(message, errorCode);
    }

    public static SessionOperationException cannotCancelInactive() {
        return new SessionOperationException("Cannot cancel inactive session", "CANNOT_CANCEL_INACTIVE");
    }

    public static SessionOperationException cannotCancelTooLate() {
        return new SessionOperationException("Cannot cancel session less than 1 hour before start",
                "CANNOT_CANCEL_TOO_LATE");
    }

    public static SessionOperationException onlyCancelledCanBeReactivated() {
        return new SessionOperationException("Only cancelled sessions can be reactivated",
                "ONLY_CANCELLED_CAN_REACTIVATE");
    }

    public static SessionOperationException cannotDeleteWithBookings() {
        return new SessionOperationException("Cannot delete a session that has bookings, cancel it instead",
                "CANNOT_DELETE_WITH_BOOKINGS");
    }

    public static SessionOperationException cannotRescheduleWithReservations() {
        return new SessionOperationException(
                "Cannot change the movie, hall or start time of a session with booked or held seats",
                "CANNOT_RESCHEDULE_WITH_RESERVATIONS");
    }

    public static SessionOperationException cannotEditStarted() {
        return new SessionOperationException("Cannot edit a session that has already started or completed",
                "CANNOT_EDIT_STARTED_SESSION");
    }

    public static SessionOperationException cannotReactivatePast() {
        return new SessionOperationException("Cannot reactivate past session", "CANNOT_REACTIVATE_PAST");
    }
}