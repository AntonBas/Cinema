package ua.lviv.bas.cinema.exception.domain.technical;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

import java.io.Serial;

public class QRCodeGenerationException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public QRCodeGenerationException(String message, Throwable cause) {
        super(message, "QR_CODE_GENERATION_FAILED", HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to generate QR code: " + (cause != null ? cause.getMessage() : ""), cause);
    }
}