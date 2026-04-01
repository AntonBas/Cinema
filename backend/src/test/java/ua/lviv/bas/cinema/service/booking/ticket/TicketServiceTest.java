package ua.lviv.bas.cinema.service.booking.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketValidationException;
import ua.lviv.bas.cinema.repository.ticket.TicketRepository;
import ua.lviv.bas.cinema.service.integration.qr.QRCodeService;
import ua.lviv.bas.cinema.service.shared.NumberGeneratorService;
import ua.lviv.bas.cinema.service.ticket.TicketService;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private TicketValidationService validationService;

	@Mock
	private QRCodeService qrCodeService;

	@Mock
	private NumberGeneratorService numberGenerator;

	@InjectMocks
	private TicketService ticketService;

	private final String TICKET_CODE = "TICKET-123";
	private final String GENERATED_CODE = "TICKET-GEN-456";
	private final BigDecimal PRICE = new BigDecimal("150.00");
	private final byte[] QR_CODE = new byte[] { 1, 2, 3 };

	@Test
	void createTicketsForBooking_Success() {
		Booking booking = createBooking();
		Payment payment = createPayment();
		SeatReservation seatReservation = createSeatReservation();

		booking.setSeatReservations(List.of(seatReservation));

		when(numberGenerator.generateTicketCode()).thenReturn(GENERATED_CODE);
		when(ticketRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

		List<Ticket> tickets = ticketService.createTicketsForBooking(booking, payment);

		assertThat(tickets).hasSize(1);
		Ticket ticket = tickets.get(0);
		assertThat(ticket.getUniqueCode()).isEqualTo(GENERATED_CODE);
		assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ACTIVE);
		assertThat(ticket.getOriginalPrice()).isEqualTo(PRICE);
		assertThat(ticket.getFinalPrice()).isEqualTo(PRICE);
		verify(ticketRepository).saveAll(anyList());
	}

	@Test
	void validateTicket_Success() {
		Ticket ticket = createTicket(TICKET_CODE, TicketStatus.ACTIVE);

		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(ticket));
		when(ticketRepository.save(ticket)).thenReturn(ticket);

		ticketService.validateTicket(TICKET_CODE);

		assertThat(ticket.getStatus()).isEqualTo(TicketStatus.USED);
		verify(validationService).validateTicketForEntry(ticket);
		verify(ticketRepository).save(ticket);
	}

	@Test
	void validateTicket_NotFound_ThrowsException() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketService.validateTicket(TICKET_CODE))
				.isInstanceOf(TicketValidationException.class);
	}

	@Test
	void generateTicketQRCode_Success() {
		ReflectionTestUtils.setField(ticketService, "ticketBaseUrl", "http://localhost:8080");
		ReflectionTestUtils.setField(ticketService, "qrCodeSize", 200);

		String expectedQrContent = "http://localhost:8080/api/tickets/validate/" + TICKET_CODE;
		when(qrCodeService.generateQRCode(expectedQrContent, 200)).thenReturn(QR_CODE);

		byte[] result = ticketService.generateTicketQRCode(TICKET_CODE);

		assertThat(result).isEqualTo(QR_CODE);
		verify(qrCodeService).generateQRCode(expectedQrContent, 200);
	}

	@Test
	void isTicketValid_ValidTicket_ReturnsTrue() {
		Ticket ticket = createTicket(TICKET_CODE, TicketStatus.ACTIVE);

		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(ticket));
		when(validationService.isTicketValidForEntry(ticket)).thenReturn(true);

		boolean result = ticketService.isTicketValid(TICKET_CODE);

		assertThat(result).isTrue();
	}

	@Test
	void isTicketValid_InvalidTicket_ReturnsFalse() {
		Ticket ticket = createTicket(TICKET_CODE, TicketStatus.ACTIVE);

		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(ticket));
		when(validationService.isTicketValidForEntry(ticket)).thenReturn(false);

		boolean result = ticketService.isTicketValid(TICKET_CODE);

		assertThat(result).isFalse();
	}

	@Test
	void isTicketValid_TicketNotFound_ReturnsFalse() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

		boolean result = ticketService.isTicketValid(TICKET_CODE);

		assertThat(result).isFalse();
	}

	@Test
	void checkTicketStatus_Success() {
		Ticket ticket = createTicket(TICKET_CODE, TicketStatus.ACTIVE);

		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(ticket));

		TicketStatus result = ticketService.checkTicketStatus(TICKET_CODE);

		assertThat(result).isEqualTo(TicketStatus.ACTIVE);
	}

	@Test
	void checkTicketStatus_TicketNotFound_ReturnsNull() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

		TicketStatus result = ticketService.checkTicketStatus(TICKET_CODE);

		assertThat(result).isNull();
	}

	private Booking createBooking() {
		User user = new User();
		user.setId(1L);
		user.setEmail("user@example.com");

		Booking booking = new Booking();
		booking.setId(1L);
		booking.setUser(user);
		return booking;
	}

	private Payment createPayment() {
		Payment payment = new Payment();
		payment.setId(1L);
		return payment;
	}

	private SeatReservation createSeatReservation() {
		SeatReservation reservation = new SeatReservation();
		reservation.setSeatPrice(PRICE);
		return reservation;
	}

	private Ticket createTicket(String code, TicketStatus status) {
		Ticket ticket = new Ticket();
		ticket.setId(1L);
		ticket.setUniqueCode(code);
		ticket.setStatus(status);
		ticket.setPurchaseTime(LocalDateTime.now());
		return ticket;
	}
}