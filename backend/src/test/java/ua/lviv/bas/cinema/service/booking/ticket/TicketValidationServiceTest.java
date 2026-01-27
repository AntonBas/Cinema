package ua.lviv.bas.cinema.service.booking.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketValidationException;

public class TicketValidationServiceTest {

	private TicketValidationService ticketValidationService;

	@BeforeEach
	void setUp() {
		ticketValidationService = new TicketValidationService();
	}

	@Test
	void validateTicketForEntry_Success() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().plusHours(2));
		session.setStatus(CinemaSessionStatus.SCHEDULED);

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		ticketValidationService.validateTicketForEntry(ticket);
	}

	@Test
	void validateTicketForEntry_WhenTicketAlreadyUsed_ShouldThrowException() {
		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.USED);

		assertThatThrownBy(() -> ticketValidationService.validateTicketForEntry(ticket))
				.isInstanceOf(TicketValidationException.class).hasMessageContaining("Ticket has already been used");
	}

	@Test
	void validateTicketForEntry_WhenTicketCancelled_ShouldThrowException() {
		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.CANCELLED);

		assertThatThrownBy(() -> ticketValidationService.validateTicketForEntry(ticket))
				.isInstanceOf(TicketValidationException.class).hasMessageContaining("Ticket has been cancelled");
	}

	@Test
	void validateTicketForEntry_WhenTicketNotActive_ShouldThrowException() {
		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.REFUNDED);

		assertThatThrownBy(() -> ticketValidationService.validateTicketForEntry(ticket))
				.isInstanceOf(TicketValidationException.class).hasMessageContaining("Ticket is not active");
	}

	@Test
	void validateTicketForEntry_WhenSessionAlreadyStarted_ShouldThrowException() {
		Session session = new Session();
		// Сеанс розпочався 1 годину тому
		session.setStartTime(LocalDateTime.now().minusHours(1));
		session.setStatus(CinemaSessionStatus.SCHEDULED);

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		assertThatThrownBy(() -> ticketValidationService.validateTicketForEntry(ticket))
				.isInstanceOf(TicketValidationException.class).hasMessageContaining("Session has already started");
	}

	@Test
	void validateTicketForEntry_WhenSessionEndedMoreThan2HoursAgo_ShouldThrowException() {
		Session session = new Session();
		// Сеанс розпочався 3 години тому
		session.setStartTime(LocalDateTime.now().minusHours(3));
		session.setStatus(CinemaSessionStatus.SCHEDULED);

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		assertThatThrownBy(() -> ticketValidationService.validateTicketForEntry(ticket))
				.isInstanceOf(TicketValidationException.class).hasMessageContaining("Session has already started");
	}

	@Test
	void validateTicketForEntry_WhenSessionCancelled_ShouldThrowException() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().plusHours(2));
		session.setStatus(CinemaSessionStatus.CANCELLED);

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		assertThatThrownBy(() -> ticketValidationService.validateTicketForEntry(ticket))
				.isInstanceOf(TicketValidationException.class).hasMessageContaining("Session has been cancelled");
	}

	@Test
	void isTicketValidForEntry_Success() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().plusHours(2));
		session.setStatus(CinemaSessionStatus.SCHEDULED);

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		boolean result = ticketValidationService.isTicketValidForEntry(ticket);

		assertThat(result).isTrue();
	}

	@Test
	void isTicketValidForEntry_WhenInvalid() {
		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.USED);

		boolean result = ticketValidationService.isTicketValidForEntry(ticket);

		assertThat(result).isFalse();
	}
}