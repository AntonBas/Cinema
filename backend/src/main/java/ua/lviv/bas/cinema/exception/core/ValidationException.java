package ua.lviv.bas.cinema.exception.core;

import jakarta.annotation.Nullable;
import org.springframework.http.HttpStatus;

import java.io.Serial;

public abstract class ValidationException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ValidationException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST, null);
    }

    public ValidationException(String message, String errorCode, @Nullable String debugMessage) {
        super(message, errorCode, HttpStatus.BAD_REQUEST, debugMessage);
    }

    public ValidationException(String message, String errorCode, @Nullable String debugMessage,
                               @Nullable Throwable cause) {
        super(message, errorCode, HttpStatus.BAD_REQUEST, debugMessage, cause);
    }
}