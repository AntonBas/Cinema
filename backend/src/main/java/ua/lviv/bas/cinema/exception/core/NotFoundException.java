package ua.lviv.bas.cinema.exception.core;

import jakarta.annotation.Nullable;
import org.springframework.http.HttpStatus;

import java.io.Serial;

public abstract class NotFoundException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public NotFoundException(String message, String errorCode, @Nullable String debugMessage) {
        super(message, errorCode, HttpStatus.NOT_FOUND, debugMessage);
    }

    public NotFoundException(String message, String errorCode, @Nullable String debugMessage,
                             @Nullable Throwable cause) {
        super(message, errorCode, HttpStatus.NOT_FOUND, debugMessage, cause);
    }
}