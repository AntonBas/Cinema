package ua.lviv.bas.cinema.service.booking.ticket;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketValidationException;

@Service
@RequiredArgsConstructor
public class TicketValidationService {

	public void validateTicketForEntry(Ticket ticket) {
		if (ticket.getStatus() == TicketStatus.USED) {
			throw TicketValidationException.alreadyUsed();
		}

		if (ticket.getStatus() == TicketStatus.REFUNDED) {
			throw new TicketValidationException("Ticket has been refunded");
		}

		if (ticket.getStatus() != TicketStatus.ACTIVE) {
			throw new TicketValidationException("Ticket is not active");
		}

		Session session = ticket.getBooking().getSession();

		if (session.getStartTime().isAfter(LocalDateTime.now())) {
			throw new TicketValidationException("Session has not started yet");
		}

		if (session.getStartTime().isBefore(LocalDateTime.now().minusHours(2))) {
			throw new TicketValidationException("Session ended more than 2 hours ago");
		}

		if (session.getStatus() == CinemaSessionStatus.CANCELLED) {
			throw new TicketValidationException("Session has been cancelled");
		}
	}

	public boolean isTicketValidForEntry(Ticket ticket) {
		try {
			validateTicketForEntry(ticket);
			return true;
		} catch (TicketValidationException e) {
			return false;
		}
	}
}