package ua.lviv.bas.cinema.exception.domain.ticket;

import ua.lviv.bas.cinema.exception.core.ValidationException;

import java.io.Serial;

public class TicketTypeValidationException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TicketTypeValidationException(String message, String debugMessage) {
        super(message, "TICKET_TYPE_VALIDATION_FAILED", debugMessage);
    }

    public static TicketTypeValidationException invalidAgeRange(Integer minAge, Integer maxAge) {
        return new TicketTypeValidationException(
                String.format("Invalid age range: minimum age (%d) cannot be greater than maximum age (%d)", minAge, maxAge),
                String.format("Invalid age range: minAge=%d > maxAge=%d", minAge, maxAge));
    }

    public static TicketTypeValidationException invalidAgeValue(String fieldName, Integer age) {
        return new TicketTypeValidationException(
                String.format("Invalid value for field '%s': %d. Age must be between 0-100 years", fieldName, age),
                String.format("Invalid %s value: %d, must be 0-100", fieldName, age));
    }
}