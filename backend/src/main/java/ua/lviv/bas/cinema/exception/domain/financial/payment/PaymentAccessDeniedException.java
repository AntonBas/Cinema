package ua.lviv.bas.cinema.exception.domain.financial.payment;

import org.springframework.http.HttpStatus;
import ua.lviv.bas.cinema.exception.core.BusinessException;

import java.io.Serial;

public class PaymentAccessDeniedException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PaymentAccessDeniedException(Long paymentId, Long userId) {
        super("Access denied to this payment", "PAYMENT_ACCESS_DENIED", HttpStatus.FORBIDDEN,
                String.format("User ID: %d, Payment ID: %d", userId, paymentId));
    }
}