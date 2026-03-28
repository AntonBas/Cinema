package ua.lviv.bas.cinema.exception.domain.financial.payment;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class PaymentAccessDeniedException extends ValidationException {
	private static final long serialVersionUID = 1L;

	public PaymentAccessDeniedException(Long paymentId, Long userId) {
		super(String.format("User %d does not have access to payment %d", userId, paymentId), "PAYMENT_ACCESS_DENIED",
				String.format("User ID: %d, Payment ID: %d", userId, paymentId));
	}
}