package ua.lviv.bas.cinema.exception.core;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import java.io.Serial;

public abstract class ConflictException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ConflictException(String message, String errorCode, @Nullable String debugMessage) {
        super(message, errorCode, HttpStatus.CONFLICT, debugMessage);
    }

    public ConflictException(String message, String errorCode, @Nullable String debugMessage,
                             @Nullable Throwable cause) {
        super(message, errorCode, HttpStatus.CONFLICT, debugMessage, cause);
    }
}