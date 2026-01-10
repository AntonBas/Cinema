package ua.lviv.bas.cinema.exception.domain.payment;

import ua.lviv.bas.cinema.domain.enums.PaymentStatus;
import ua.lviv.bas.cinema.exception.core.ValidationException;

public class InvalidPaymentStatusException extends ValidationException {
	private static final long serialVersionUID = 1L;

	public InvalidPaymentStatusException(PaymentStatus current, PaymentStatus required) {
		super(String.format("Payment status must be %s, but is %s", required, current), "INVALID_PAYMENT_STATUS",
				String.format("Current: %s, Required: %s", current, required));
	}

	public static InvalidPaymentStatusException notPending(PaymentStatus current) {
		return new InvalidPaymentStatusException(current, PaymentStatus.PENDING);
	}

	public static InvalidPaymentStatusException notFailed(PaymentStatus current) {
		return new InvalidPaymentStatusException(current, PaymentStatus.FAILED);
	}
}