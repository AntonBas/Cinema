package ua.lviv.bas.cinema.exception.domain.financial.payment;

import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.exception.core.ValidationException;

import java.io.Serial;

public class InvalidPaymentStatusException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidPaymentStatusException(PaymentStatus current, PaymentStatus required) {
        this(String.format("Payment status must be %s, but is %s", required, current),
                String.format("Current: %s, Required: %s", current, required));
    }

    private InvalidPaymentStatusException(String message, String debugMessage) {
        super(message, "INVALID_PAYMENT_STATUS", debugMessage);
    }

    public static InvalidPaymentStatusException alreadyCompleted(PaymentStatus current) {
        return new InvalidPaymentStatusException("This booking already has a completed payment",
                String.format("Current: %s", current));
    }

    public static InvalidPaymentStatusException notFailed(PaymentStatus current) {
        return new InvalidPaymentStatusException(current, PaymentStatus.FAILED);
    }
}