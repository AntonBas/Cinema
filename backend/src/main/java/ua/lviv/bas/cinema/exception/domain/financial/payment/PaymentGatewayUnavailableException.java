package ua.lviv.bas.cinema.exception.domain.financial.payment;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import ua.lviv.bas.cinema.exception.core.BusinessException;

import java.io.Serial;

public class PaymentGatewayUnavailableException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PaymentGatewayUnavailableException(String message, @Nullable Throwable cause) {
        super(message, "PAYMENT_GATEWAY_UNAVAILABLE", HttpStatus.SERVICE_UNAVAILABLE,
                "LiqPay outcome could not be confirmed: " + message, cause);
    }
}
