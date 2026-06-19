package ua.lviv.bas.cinema.exception.domain.technical;

import ua.lviv.bas.cinema.exception.core.ValidationException;

import java.io.Serial;

public class SlugGenerationException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SlugGenerationException(String message, String errorCode) {
        super(message, errorCode);
    }

    public static SlugGenerationException titleRequired() {
        return new SlugGenerationException("Title cannot be null or empty", "TITLE_REQUIRED");
    }
}