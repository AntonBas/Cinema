package ua.lviv.bas.cinema.exception.infrastructure;

import ua.lviv.bas.cinema.exception.core.BusinessException;
import org.springframework.http.HttpStatus;

import java.io.Serial;

public class ExternalServiceException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ExternalServiceException(String serviceName, Throwable cause) {
        super(String.format("External service '%s' unavailable", serviceName), "EXTERNAL_SERVICE_ERROR",
                HttpStatus.SERVICE_UNAVAILABLE, cause.getMessage());
    }
}