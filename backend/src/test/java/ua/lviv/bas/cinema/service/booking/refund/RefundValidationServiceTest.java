package ua.lviv.bas.cinema.service.booking.refund;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.config.RefundRules;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.Refund;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;

@ExtendWith(MockitoExtension.class)
public class RefundValidationServiceTest {

	@Mock
	private RefundRules refundRules;

	private RefundValidationService refundValidationService;

	@BeforeEach
	void setUp() {
		refundValidationService = new RefundValidationService(refundRules);
	}

	@Test
	void validateRefund_Success() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().plusHours(3));

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		when(refundRules.isRefundable(session.getStartTime())).thenReturn(true);

		String result = refundValidationService.validateRefund(ticket);

		assertThat(result).isNull();
	}

	@Test
	void validateRefund_WhenTicketNotActive() {
		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.REFUNDED);

		String result = refundValidationService.validateRefund(ticket);

		assertThat(result).contains("Ticket is not active");
	}

	@Test
	void validateRefund_WhenRefundNotAvailable() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().plusHours(3));

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		when(refundRules.isRefundable(session.getStartTime())).thenReturn(false);

		String result = refundValidationService.validateRefund(ticket);

		assertThat(result).contains("Refund is not available for this session");
	}

	@Test
	void validateRefund_WhenTicketAlreadyRefunded() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().plusHours(3));

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);
		ticket.setRefund(new Refund());

		when(refundRules.isRefundable(session.getStartTime())).thenReturn(true);

		String result = refundValidationService.validateRefund(ticket);

		assertThat(result).contains("Ticket has already been refunded");
	}

	@Test
	void validateRefund_WhenSessionAlreadyStarted() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().minusHours(1));

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		when(refundRules.isRefundable(session.getStartTime())).thenReturn(true);

		String result = refundValidationService.validateRefund(ticket);

		assertThat(result).contains("Session has already started or finished");
	}

	@Test
	void validateRefund_WhenSessionWithin3Hours() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().plusHours(2));

		Booking booking = new Booking();
		booking.setSession(session);

		Ticket ticket = new Ticket();
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setBooking(booking);

		when(refundRules.isRefundable(session.getStartTime())).thenReturn(false);

		String result = refundValidationService.validateRefund(ticket);

		assertThat(result).contains("Refund is not available for this session");
	}
}