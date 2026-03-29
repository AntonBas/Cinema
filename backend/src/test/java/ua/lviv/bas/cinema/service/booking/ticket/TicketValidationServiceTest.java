package ua.lviv.bas.cinema.service.booking.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
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
		session.setStartTime(LocalDateTime.now().minusHours(1));
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
				.isInstanceOf(TicketValidationException.class).hasMessage("Ticket has already been used");
	}

	@Test
	void validateTicketForEntry_WhenTicketRefunded_ShouldThrowException() {
		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.REFUNDED);

		assertThatThrownBy(() -> ticketValidationService.validateTicketForEntry(ticket))
				.isInstanceOf(TicketValidationException.class).hasMessage("Ticket has been refunded");
	}

	@Test
	void validateTicketForEntry_WhenSessionNotStarted_ShouldThrowException() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().plusHours(1));
		session.setStatus(CinemaSessionStatus.SCHEDULED);

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		assertThatThrownBy(() -> ticketValidationService.validateTicketForEntry(ticket))
				.isInstanceOf(TicketValidationException.class).hasMessage("Session has not started yet");
	}

	@Test
	void validateTicketForEntry_WhenSessionEndedMoreThan2HoursAgo_ShouldThrowException() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().minusHours(3));
		session.setStatus(CinemaSessionStatus.SCHEDULED);

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		assertThatThrownBy(() -> ticketValidationService.validateTicketForEntry(ticket))
				.isInstanceOf(TicketValidationException.class).hasMessage("Session ended more than 2 hours ago");
	}

	@Test
	void validateTicketForEntry_WhenSessionEndedLessThan2HoursAgo_ShouldPass() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().minusMinutes(90));
		session.setStatus(CinemaSessionStatus.SCHEDULED);

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		ticketValidationService.validateTicketForEntry(ticket);
	}

	@Test
	void validateTicketForEntry_WhenSessionEndedExactly2HoursAgo_ShouldThrowException() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().minusHours(2));
		session.setStatus(CinemaSessionStatus.SCHEDULED);

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		assertThatThrownBy(() -> ticketValidationService.validateTicketForEntry(ticket))
				.isInstanceOf(TicketValidationException.class).hasMessage("Session ended more than 2 hours ago");
	}

	@Test
	void validateTicketForEntry_WhenSessionCancelled_ShouldThrowException() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().minusHours(1));
		session.setStatus(CinemaSessionStatus.CANCELLED);

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		assertThatThrownBy(() -> ticketValidationService.validateTicketForEntry(ticket))
				.isInstanceOf(TicketValidationException.class).hasMessage("Session has been cancelled");
	}

	@Test
	void validateTicketForEntry_WhenSessionStartedNow_ShouldPass() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now());
		session.setStatus(CinemaSessionStatus.SCHEDULED);

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		ticketValidationService.validateTicketForEntry(ticket);
	}

	@Test
	void validateTicketForEntry_WhenSessionStarted1MinuteAgo_ShouldPass() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().minusMinutes(1));
		session.setStatus(CinemaSessionStatus.SCHEDULED);

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		ticketValidationService.validateTicketForEntry(ticket);
	}

	@Test
	void validateTicketForEntry_WhenSessionStarted2HoursAnd1MinuteAgo_ShouldThrowException() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().minusHours(2).minusMinutes(1));
		session.setStatus(CinemaSessionStatus.SCHEDULED);

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		assertThatThrownBy(() -> ticketValidationService.validateTicketForEntry(ticket))
				.isInstanceOf(TicketValidationException.class).hasMessage("Session ended more than 2 hours ago");
	}

	@Test
	void validateTicketForEntry_WhenSessionStarted1MinuteBeforeNow_ShouldThrowException() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().minusSeconds(59));
		session.setStatus(CinemaSessionStatus.SCHEDULED);

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		ticketValidationService.validateTicketForEntry(ticket);
	}

	@Test
	void validateTicketForEntry_WhenSessionStatusCompleted_ShouldPass() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().minusHours(1));
		session.setStatus(CinemaSessionStatus.COMPLETED);

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		ticketValidationService.validateTicketForEntry(ticket);
	}

	@Test
	void isTicketValidForEntry_WhenValid_ReturnsTrue() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().minusHours(1));
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
	void isTicketValidForEntry_WhenTicketUsed_ReturnsFalse() {
		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.USED);

		boolean result = ticketValidationService.isTicketValidForEntry(ticket);

		assertThat(result).isFalse();
	}

	@Test
	void isTicketValidForEntry_WhenTicketRefunded_ReturnsFalse() {
		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.REFUNDED);

		boolean result = ticketValidationService.isTicketValidForEntry(ticket);

		assertThat(result).isFalse();
	}

	@Test
	void isTicketValidForEntry_WhenSessionNotStarted_ReturnsFalse() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().plusHours(1));
		session.setStatus(CinemaSessionStatus.SCHEDULED);

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		boolean result = ticketValidationService.isTicketValidForEntry(ticket);

		assertThat(result).isFalse();
	}

	@Test
	void isTicketValidForEntry_WhenSessionCancelled_ReturnsFalse() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().minusHours(1));
		session.setStatus(CinemaSessionStatus.CANCELLED);

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		boolean result = ticketValidationService.isTicketValidForEntry(ticket);

		assertThat(result).isFalse();
	}

	@Test
	void isTicketValidForEntry_WhenSessionEndedExactly2HoursAgo_ReturnsFalse() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().minusHours(2));
		session.setStatus(CinemaSessionStatus.SCHEDULED);

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		boolean result = ticketValidationService.isTicketValidForEntry(ticket);

		assertThat(result).isFalse();
	}

	@Test
	void isTicketValidForEntry_WhenSessionEndedMoreThan2HoursAgo_ReturnsFalse() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().minusHours(3));
		session.setStatus(CinemaSessionStatus.SCHEDULED);

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		boolean result = ticketValidationService.isTicketValidForEntry(ticket);

		assertThat(result).isFalse();
	}

	@Test
	void validateTicketForEntry_WhenBookingNull_ShouldThrowNullPointerException() {
		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(null);

		assertThatThrownBy(() -> ticketValidationService.validateTicketForEntry(ticket))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	void validateTicketForEntry_WhenSessionNull_ShouldThrowNullPointerException() {
		Booking booking = new Booking();
		booking.setSession(null);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		assertThatThrownBy(() -> ticketValidationService.validateTicketForEntry(ticket))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	void validateTicketForEntry_WhenSessionStartTimeNull_ShouldThrowNullPointerException() {
		Session session = new Session();
		session.setStartTime(null);
		session.setStatus(CinemaSessionStatus.SCHEDULED);

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		assertThatThrownBy(() -> ticketValidationService.validateTicketForEntry(ticket))
				.isInstanceOf(NullPointerException.class);
	}
}