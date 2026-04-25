package ua.lviv.bas.cinema.exception.domain.financial.payment;

import ua.lviv.bas.cinema.domain.booking.status.PaymentStatus;
import ua.lviv.bas.cinema.exception.core.ValidationException;

import java.io.Serial;

public class InvalidPaymentStatusException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidPaymentStatusException(PaymentStatus current, PaymentStatus required) {
        super(String.format("Payment status must be %s, but is %s", required, current), "INVALID_PAYMENT_STATUS",
                String.format("Current: %s, Required: %s", current, required));
    }

    public static InvalidPaymentStatusException notFailed(PaymentStatus current) {
        return new InvalidPaymentStatusException(current, PaymentStatus.FAILED);
    }
}