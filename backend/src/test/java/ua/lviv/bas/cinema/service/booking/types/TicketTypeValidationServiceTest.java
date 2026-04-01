package ua.lviv.bas.cinema.service.booking.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import ua.lviv.bas.cinema.domain.ticket.TicketType;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketTypeValidationException;
import ua.lviv.bas.cinema.service.ticket.TicketTypeValidationService;

public class TicketTypeValidationServiceTest {

	private final TicketTypeValidationService validationService = new TicketTypeValidationService();

	@Test
	void validateAgeRange_ValidRange_ShouldNotThrowException() {
		validationService.validateAgeRange(18, 65);
		validationService.validateAgeRange(null, 65);
		validationService.validateAgeRange(18, null);
		validationService.validateAgeRange(null, null);
	}

	@Test
	void validateAgeRange_WhenMinGreaterThanMax_ShouldThrowException() {
		assertThatThrownBy(() -> validationService.validateAgeRange(25, 15))
				.isInstanceOf(TicketTypeValidationException.class);
	}

	@Test
	void validateAgeRange_WhenMinAgeInvalid_ShouldThrowException() {
		assertThatThrownBy(() -> validationService.validateAgeRange(-5, 50))
				.isInstanceOf(TicketTypeValidationException.class);
	}

	@Test
	void validateAgeRange_WhenMaxAgeInvalid_ShouldThrowException() {
		assertThatThrownBy(() -> validationService.validateAgeRange(18, -5))
				.isInstanceOf(TicketTypeValidationException.class);
	}

	@ParameterizedTest
	@CsvSource({ "18, 65, 25, true", "18, 65, 18, true", "18, 65, 65, true", "18, 65, 17, false", "18, 65, 66, false",
			", 65, 25, true", ", 65, 70, false", "18, , 25, true", "18, , 15, false", ", , 25, true" })
	void isAgeValidForTicketType_VariousScenarios(Integer minAge, Integer maxAge, Integer age, boolean expected) {
		TicketType ticketType = TicketType.builder().minAge(minAge).maxAge(maxAge).build();

		boolean result = validationService.isAgeValidForTicketType(ticketType, age);
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void isAgeValidForTicketType_WhenAgeNullAndNoRestrictions_ShouldReturnTrue() {
		TicketType ticketType = TicketType.builder().minAge(null).maxAge(null).build();

		boolean result = validationService.isAgeValidForTicketType(ticketType, null);
		assertThat(result).isTrue();
	}

	@ParameterizedTest
	@CsvSource({ ", , 'No age restrictions'", "18, 65, '18-65 years'", "18, , 'From 18 years'",
			", 65, 'Up to 65 years'" })
	void formatAgeRange_VariousScenarios(Integer minAge, Integer maxAge, String expected) {
		String result = validationService.formatAgeRange(minAge, maxAge);
		assertThat(result).isEqualTo(expected);
	}
}