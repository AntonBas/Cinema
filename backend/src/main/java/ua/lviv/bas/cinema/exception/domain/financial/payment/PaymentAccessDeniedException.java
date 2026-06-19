package ua.lviv.bas.cinema.exception.domain.financial.payment;

import ua.lviv.bas.cinema.exception.core.ValidationException;

import java.io.Serial;

public class PaymentAccessDeniedException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PaymentAccessDeniedException(Long paymentId, Long userId) {
        super("Access denied to this payment", "PAYMENT_ACCESS_DENIED",
                String.format("User ID: %d, Payment ID: %d", userId, paymentId));
    }
}