package ua.lviv.bas.cinema.exception.infrastructure;

import ua.lviv.bas.cinema.exception.core.ValidationException;

import java.io.Serial;

public class UnsupportedFileTypeException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UnsupportedFileTypeException(String contentType) {
        super("Unsupported file type: " + contentType, "UNSUPPORTED_FILE_TYPE");
    }
}
