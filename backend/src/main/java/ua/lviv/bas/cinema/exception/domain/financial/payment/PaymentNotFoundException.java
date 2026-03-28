package ua.lviv.bas.cinema.exception.domain.financial.payment;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

public class PaymentNotFoundException extends NotFoundException {
	private static final long serialVersionUID = 1L;

	public PaymentNotFoundException(Long paymentId) {
		super(String.format("Payment with ID %d not found", paymentId), "PAYMENT_NOT_FOUND",
				String.format("Payment ID: %d", paymentId));
	}

	public PaymentNotFoundException(String orderId) {
		super(String.format("Payment with order ID %s not found", orderId), "PAYMENT_NOT_FOUND",
				String.format("Order ID: %s", orderId));
	}
}