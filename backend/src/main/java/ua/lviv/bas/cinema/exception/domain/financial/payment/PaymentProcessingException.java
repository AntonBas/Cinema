package ua.lviv.bas.cinema.exception.domain.financial.payment;

import ua.lviv.bas.cinema.exception.core.ValidationException;

import java.io.Serial;

public class PaymentProcessingException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PaymentProcessingException(String message) {
        super(message, "PAYMENT_PROCESSING_ERROR");
    }

    public PaymentProcessingException(String message, String errorCode) {
        super(message, errorCode);
    }

    public static PaymentProcessingException bookingNotPending() {
        return new PaymentProcessingException("Booking is not in PENDING status", "BOOKING_NOT_PENDING");
    }

    public static PaymentProcessingException bookingExpired() {
        return new PaymentProcessingException("Booking has expired", "BOOKING_EXPIRED");
    }

    public static PaymentProcessingException seatsNoLongerAvailable() {
        return new PaymentProcessingException("Some seats are no longer available", "SEATS_NO_LONGER_AVAILABLE");
    }

    public static PaymentProcessingException refundFailed(String error) {
        return new PaymentProcessingException("Refund failed: " + error, "REFUND_FAILED");
    }
}