package ua.lviv.bas.cinema.exception.domain.ticket;

import org.springframework.lang.Nullable;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class TicketTypeValidationException extends ValidationException {

	private static final long serialVersionUID = 1L;
	private static final String ERROR_CODE = "TICKET_TYPE_VALIDATION_FAILED";

	public TicketTypeValidationException(String message, @Nullable String debugMessage) {
		super(message, ERROR_CODE, debugMessage);
	}

	public static TicketTypeValidationException invalidAgeRange(Integer minAge, Integer maxAge) {
		return new TicketTypeValidationException(String
				.format("Invalid age range: minimum age (%d) cannot be greater than maximum age (%d)", minAge, maxAge),
				String.format("Invalid age range: minAge=%d > maxAge=%d", minAge, maxAge));
	}

	public static TicketTypeValidationException invalidAgeValue(String fieldName, Integer age) {
		return new TicketTypeValidationException(
				String.format("Invalid value for field '%s': %d. Age must be between 0-100 years", fieldName, age),
				String.format("Invalid %s value: %d, must be 0-100", fieldName, age));
	}
}