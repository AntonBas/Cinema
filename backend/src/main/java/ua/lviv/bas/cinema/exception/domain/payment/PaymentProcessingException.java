package ua.lviv.bas.cinema.exception.domain.payment;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class PaymentProcessingException extends ValidationException {

	private static final long serialVersionUID = 1L;

	public PaymentProcessingException(String message) {
		super(message, "PAYMENT_PROCESSING_ERROR");
	}

	public PaymentProcessingException(String message, String errorCode) {
		super(message, errorCode);
	}

	public PaymentProcessingException(String message, Throwable cause) {
		super(message, "PAYMENT_PROCESSING_ERROR", message, cause);
	}

	public PaymentProcessingException(String message, String errorCode, Throwable cause) {
		super(message, errorCode, message, cause);
	}

	public static PaymentProcessingException paymentInProgress() {
		return new PaymentProcessingException("Payment is already in progress for this booking", "PAYMENT_IN_PROGRESS");
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