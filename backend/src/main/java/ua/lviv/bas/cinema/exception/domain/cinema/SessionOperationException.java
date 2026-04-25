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

    public static SessionOperationException cannotReactivatePast() {
        return new SessionOperationException("Cannot reactivate past session", "CANNOT_REACTIVATE_PAST");
    }
}