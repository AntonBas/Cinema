package ua.lviv.bas.cinema.exception.core;

import java.io.Serial;
import java.util.Locale;

public class EntityNotFoundException extends NotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public EntityNotFoundException(String entityType, Object identifier) {
        super(entityType + " not found", buildErrorCode(entityType),
                entityType + " entity with id " + identifier + " does not exist");
    }

    private static String buildErrorCode(String entityType) {
        return entityType.toUpperCase(Locale.ROOT).replace(' ', '_') + "_NOT_FOUND";
    }
}
