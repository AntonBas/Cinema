package ua.lviv.bas.cinema.service.booking.types;

import org.springframework.stereotype.Service;

import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketTypeValidationException;

@Service
public class TicketTypeValidationService {

	public void validateAgeRange(Integer minAge, Integer maxAge) {
		if (minAge != null && maxAge != null && minAge > maxAge) {
			throw TicketTypeValidationException.invalidAgeRange(minAge, maxAge);
		}
		if (minAge != null && (minAge < 0 || minAge > 100)) {
			throw TicketTypeValidationException.invalidAgeValue("minAge", minAge);
		}
		if (maxAge != null && (maxAge < 0 || maxAge > 100)) {
			throw TicketTypeValidationException.invalidAgeValue("maxAge", maxAge);
		}
	}

	public boolean isAgeValidForTicketType(TicketType ticketType, Integer age) {
		if (age == null) {
			return ticketType.getMinAge() == null && ticketType.getMaxAge() == null;
		}
		boolean validMin = ticketType.getMinAge() == null || age >= ticketType.getMinAge();
		boolean validMax = ticketType.getMaxAge() == null || age <= ticketType.getMaxAge();
		return validMin && validMax;
	}

	public String formatAgeRange(Integer minAge, Integer maxAge) {
		if (minAge == null && maxAge == null) {
			return "No age restrictions";
		}
		if (minAge != null && maxAge != null) {
			return minAge + "-" + maxAge + " years";
		}
		if (minAge != null) {
			return "From " + minAge + " years";
		}
		return "Up to " + maxAge + " years";
	}
}