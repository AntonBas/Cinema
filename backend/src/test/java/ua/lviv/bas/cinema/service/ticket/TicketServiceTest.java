package ua.lviv.bas.cinema.service.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.domain.ticket.TicketType;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.ticket.request.TicketFilterRequest;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketNotFoundException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketValidationException;
import ua.lviv.bas.cinema.mapper.ticket.TicketMapper;
import ua.lviv.bas.cinema.repository.ticket.TicketRepository;
import ua.lviv.bas.cinema.repository.ticket.specification.TicketSpecification;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;
import ua.lviv.bas.cinema.service.integration.qr.QRCodeService;
import ua.lviv.bas.cinema.service.shared.NumberGeneratorService;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

	@Mock
	private TicketRepository ticketRepository;
	@Mock
	private TicketSpecification ticketSpecification;
	@Mock
	private TicketMapper ticketMapper;
	@Mock
	private QRCodeService qrCodeService;
	@Mock
	private NumberGeneratorService numberGenerator;
	@Mock
	private AuditService auditService;

	@InjectMocks
	private TicketService ticketService;

	private User testUser;
	private Booking testBooking;
	private Payment testPayment;
	private SeatReservation seatReservation;
	private Ticket testTicket;
	private TicketResponse testTicketResponse;
	private Session testSession;
	private Seat testSeat;
	private TicketType ticketType;

	private static final Long USER_ID = 1L;
	private static final Long OTHER_USER_ID = 2L;
	private static final Long TICKET_ID = 100L;
	private static final Long BOOKING_ID = 1L;
	private static final Long SEAT_ID = 1L;
	private static final String TICKET_CODE = "TICKET-123";
	private static final String GENERATED_CODE = "TICKET-GEN-456";
	private static final BigDecimal PRICE = new BigDecimal("150.00");
	private static final int QR_CODE_SIZE = 200;
	private static final String BASE_URL = "http://localhost:8080";

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(ticketService, "qrCodeSize", QR_CODE_SIZE);
		ReflectionTestUtils.setField(ticketService, "ticketBaseUrl", BASE_URL);

		testUser = new User();
		testUser.setId(USER_ID);
		testUser.setEmail("test@example.com");

		testSeat = new Seat();
		testSeat.setId(SEAT_ID);
		testSeat.setRow(5);
		testSeat.setNumber(12);

		Movie movie = new Movie();
		movie.setTitle("Test Movie");

		CinemaHall hall = new CinemaHall();
		hall.setName("Hall A");

		testSession = new Session();
		testSession.setId(1L);
		testSession.setMovie(movie);
		testSession.setHall(hall);
		testSession.setStartTime(LocalDateTime.now().minusHours(1));
		testSession.setStatus(CinemaSessionStatus.SCHEDULED);

		testBooking = new Booking();
		testBooking.setId(BOOKING_ID);
		testBooking.setUser(testUser);
		testBooking.setSession(testSession);

		testPayment = new Payment();
		testPayment.setId(1L);

		ticketType = new TicketType();
		ticketType.setDisplayName("Adult");

		seatReservation = new SeatReservation();
		seatReservation.setSeat(testSeat);
		seatReservation.setTicketType(ticketType);
		seatReservation.setSeatPrice(PRICE);

		testTicket = new Ticket();
		testTicket.setId(TICKET_ID);
		testTicket.setUser(testUser);
		testTicket.setBooking(testBooking);
		testTicket.setSeatReservation(seatReservation);
		testTicket.setUniqueCode(TICKET_CODE);
		testTicket.setStatus(TicketStatus.ACTIVE);
		testTicket.setOriginalPrice(PRICE);
		testTicket.setFinalPrice(PRICE);
		testTicket.setPurchaseTime(LocalDateTime.now());

		testTicketResponse = new TicketResponse(TICKET_ID, TICKET_CODE, "/api/tickets/" + TICKET_CODE + "/qr",
				TicketStatus.ACTIVE, LocalDateTime.now(), PRICE, "Adult", "Test Movie", testSession.getStartTime(),
				"Hall A", 5, 12);
	}

	@Test
	void createTicketsForBookingShouldSucceed() {
		testBooking.setSeatReservations(List.of(seatReservation));

		when(numberGenerator.generateTicketCode()).thenReturn(GENERATED_CODE);
		when(ticketRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

		List<Ticket> tickets = ticketService.createTicketsForBooking(testBooking, testPayment);

		assertThat(tickets).hasSize(1);
		Ticket ticket = tickets.get(0);
		assertThat(ticket.getUniqueCode()).isEqualTo(GENERATED_CODE);
		assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ACTIVE);
		assertThat(ticket.getOriginalPrice()).isEqualTo(PRICE);
		assertThat(ticket.getFinalPrice()).isEqualTo(PRICE);
		assertThat(ticket.getSeatReservation()).isNotNull();
		assertThat(ticket.getSeatReservation().getSeat()).isNotNull();
		assertThat(ticket.getSeatReservation().getSeat().getNumber()).isEqualTo(12);
		verify(ticketRepository).saveAll(anyList());
	}

	@Test
	void getTicketByIdShouldSucceed() {
		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.of(testTicket));
		when(ticketMapper.toTicketResponse(testTicket)).thenReturn(testTicketResponse);

		TicketResponse result = ticketService.getTicket(TICKET_ID, testUser);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(TICKET_ID);
		assertThat(result.ticketCode()).isEqualTo(TICKET_CODE);
		assertThat(result.qrCodeUrl()).isEqualTo("/api/tickets/" + TICKET_CODE + "/qr");
	}

	@Test
	void getTicketByIdWhenNotFoundShouldThrowException() {
		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketService.getTicket(TICKET_ID, testUser))
				.isInstanceOf(TicketValidationException.class);
	}

	@Test
	void getTicketByCodeShouldSucceed() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));
		when(ticketMapper.toTicketResponse(testTicket)).thenReturn(testTicketResponse);

		TicketResponse result = ticketService.getTicket(TICKET_CODE, testUser);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(TICKET_ID);
		assertThat(result.ticketCode()).isEqualTo(TICKET_CODE);
	}

	@Test
	void getTicketByCodeWhenNotFoundShouldThrowException() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketService.getTicket(TICKET_CODE, testUser))
				.isInstanceOf(TicketNotFoundException.class);
	}

	@Test
	void getTicketByCodeWithWrongUserShouldThrowException() {
		User otherUser = new User();
		otherUser.setId(OTHER_USER_ID);

		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

		assertThatThrownBy(() -> ticketService.getTicket(TICKET_CODE, otherUser))
				.isInstanceOf(TicketValidationException.class);
	}

	@Test
	void getTicketsShouldSucceed() {
		TicketFilterRequest filter = new TicketFilterRequest(TicketStatus.ACTIVE, "Test Movie");
		Pageable pageable = Pageable.unpaged();

		@SuppressWarnings("unchecked")
		Specification<Ticket> specification = (Specification<Ticket>) org.mockito.Mockito.mock(Specification.class);
		Page<Ticket> ticketPage = new PageImpl<>(List.of(testTicket));

		when(ticketSpecification.buildForUser(eq(USER_ID), eq(TicketStatus.ACTIVE), eq("Test Movie")))
				.thenReturn(specification);
		when(ticketRepository.findAll(eq(specification), eq(pageable))).thenReturn(ticketPage);
		when(ticketMapper.toTicketResponse(testTicket)).thenReturn(testTicketResponse);

		Page<TicketResponse> result = ticketService.getTickets(testUser, filter, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).id()).isEqualTo(TICKET_ID);
	}

	@Test
	void validateShouldSucceed() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));
		when(ticketRepository.save(testTicket)).thenReturn(testTicket);

		ticketService.validate(TICKET_CODE);

		assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.USED);
		verify(ticketRepository).save(testTicket);
	}

	@Test
	void validateWhenNotFoundShouldThrowException() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketService.validate(TICKET_CODE)).isInstanceOf(TicketValidationException.class);
	}

	@Test
	void validateWhenSessionNotStartedShouldThrowException() {
		testSession.setStartTime(LocalDateTime.now().plusHours(2));
		testTicket.setStatus(TicketStatus.ACTIVE);

		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

		assertThatThrownBy(() -> ticketService.validate(TICKET_CODE)).isInstanceOf(TicketValidationException.class)
				.hasMessageContaining("Session has not started yet");

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void validateWhenSessionCancelledShouldThrowException() {
		testSession.setStatus(CinemaSessionStatus.CANCELLED);
		testTicket.setStatus(TicketStatus.ACTIVE);

		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

		assertThatThrownBy(() -> ticketService.validate(TICKET_CODE)).isInstanceOf(TicketValidationException.class)
				.hasMessageContaining("Session has been cancelled");

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void validateWhenTicketAlreadyUsedShouldThrowException() {
		testTicket.setStatus(TicketStatus.USED);

		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

		assertThatThrownBy(() -> ticketService.validate(TICKET_CODE)).isInstanceOf(TicketValidationException.class)
				.hasMessageContaining("already been used");

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void validateWhenTicketRefundedShouldThrowException() {
		testTicket.setStatus(TicketStatus.REFUNDED);

		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

		assertThatThrownBy(() -> ticketService.validate(TICKET_CODE)).isInstanceOf(TicketValidationException.class)
				.hasMessageContaining("refunded");

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void generateQRShouldSucceed() {
		String expectedQrContent = BASE_URL + "/api/tickets/validate/" + TICKET_CODE;
		byte[] expectedQrCode = new byte[] { 1, 2, 3 };

		when(qrCodeService.generateQRCode(expectedQrContent, QR_CODE_SIZE)).thenReturn(expectedQrCode);

		byte[] result = ticketService.generateQR(TICKET_CODE);

		assertThat(result).isEqualTo(expectedQrCode);
		verify(qrCodeService).generateQRCode(expectedQrContent, QR_CODE_SIZE);
	}

	@Test
	void isValidWithValidTicketShouldReturnTrue() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

		boolean result = ticketService.isValid(TICKET_CODE);

		assertThat(result).isTrue();
	}

	@Test
	void isValidWithInvalidTicketShouldReturnFalse() {
		testTicket.setStatus(TicketStatus.USED);

		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

		boolean result = ticketService.isValid(TICKET_CODE);

		assertThat(result).isFalse();
	}

	@Test
	void isValidWhenTicketNotFoundShouldReturnFalse() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

		boolean result = ticketService.isValid(TICKET_CODE);

		assertThat(result).isFalse();
	}

	@Test
	void getStatusShouldSucceed() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

		TicketStatus result = ticketService.getStatus(TICKET_CODE);

		assertThat(result).isEqualTo(TicketStatus.ACTIVE);
	}

	@Test
	void getStatusWhenTicketNotFoundShouldReturnNull() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

		TicketStatus result = ticketService.getStatus(TICKET_CODE);

		assertThat(result).isNull();
	}
}