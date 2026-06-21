package ua.lviv.bas.cinema.exception.core;

import jakarta.annotation.Nullable;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;

@Getter
public abstract class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String errorCode;
    private final HttpStatus status;
    private final String debugMessage;

    public BusinessException(String message, String errorCode, HttpStatus status, @Nullable String debugMessage) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
        this.debugMessage = debugMessage;
    }

    public BusinessException(String message, String errorCode, HttpStatus status, @Nullable String debugMessage,
                             @Nullable Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.status = status;
        this.debugMessage = debugMessage;
    }

    @Nullable
    public String getDebugMessage() {
        return debugMessage;
    }
}