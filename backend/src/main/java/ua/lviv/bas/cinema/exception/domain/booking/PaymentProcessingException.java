package ua.lviv.bas.cinema.exception.domain.booking;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

public class PaymentProcessingException extends BusinessException {
	private static final long serialVersionUID = 1L;

	public PaymentProcessingException(String message) {
		super(message, "PAYMENT_ERROR", HttpStatus.BAD_REQUEST, message);
	}

	public static PaymentProcessingException bookingNotPending() {
		return new PaymentProcessingException("Booking is not in pending status");
	}

	public static PaymentProcessingException bookingExpired() {
		return new PaymentProcessingException("Booking has expired");
	}

	public static PaymentProcessingException paymentInProgress() {
		return new PaymentProcessingException("Payment already in progress");
	}
}