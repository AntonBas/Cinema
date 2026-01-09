package ua.lviv.bas.cinema.service.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import ua.lviv.bas.cinema.domain.BookedSeat;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.exception.domain.booking.TicketValidationException;
import ua.lviv.bas.cinema.mapper.TicketMapper;
import ua.lviv.bas.cinema.repository.BookingRepository;
import ua.lviv.bas.cinema.repository.TicketRepository;
import ua.lviv.bas.cinema.service.infrastructure.QRCodeService;
import ua.lviv.bas.cinema.service.notification.EmailService;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private TicketMapper ticketMapper;

	@Mock
	private EmailService emailService;

	@Mock
	private QRCodeService qrCodeService;

	@Mock
	private BookingService bookingService;

	@Mock
	private BookingRepository bookingRepository;

	@InjectMocks
	private TicketService ticketService;

	private User testUser;
	private Booking testBooking;
	private Payment testPayment;
	private Session testSession;
	private BookedSeat bookedSeat;
	private TicketType ticketType;
	private Seat seat;
	private Ticket testTicket;
	private TicketResponse ticketResponse;

	private final Long USER_ID = 1L;
	private final Long BOOKING_ID = 2L;
	private final Long PAYMENT_ID = 3L;
	private final Long TICKET_ID = 4L;
	private final Long SESSION_ID = 5L;
	private final BigDecimal BASE_PRICE = new BigDecimal("200.00");
	private final String TICKET_CODE = "TKT-ABC123DEF456";

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(ticketService, "qrCodeSize", 200);
		ReflectionTestUtils.setField(ticketService, "ticketBaseUrl", "http://localhost:8080");

		testUser = new User();
		testUser.setId(USER_ID);
		testUser.setEmail("test@example.com");

		Movie movie = new Movie();
		movie.setTitle("Test Movie");

		CinemaHall hall = new CinemaHall();
		hall.setName("Hall A");

		testSession = new Session();
		testSession.setId(SESSION_ID);
		testSession.setMovie(movie);
		testSession.setHall(hall);
		testSession.setBasePrice(BASE_PRICE);
		testSession.setStartTime(LocalDateTime.now().plusHours(2));
		testSession.setStatus(CinemaSessionStatus.SCHEDULED);

		ticketType = new TicketType();
		ticketType.setId(1L);
		ticketType.setDisplayName("Adult");
		ticketType.setPriceMultiplier(new BigDecimal("1.0"));

		seat = new Seat();
		seat.setId(1L);
		seat.setRow(1);
		seat.setNumber(1);
		seat.setSeatType(SeatType.STANDARD);

		bookedSeat = new BookedSeat();
		bookedSeat.setId(1L);
		bookedSeat.setSession(testSession);
		bookedSeat.setTicketType(ticketType);
		bookedSeat.setSeat(seat);

		testBooking = Booking.builder().id(BOOKING_ID).user(testUser).session(testSession)
				.status(BookingStatus.CONFIRMED).bookedSeats(Arrays.asList(bookedSeat)).build();

		testPayment = Payment.builder().id(PAYMENT_ID).booking(testBooking).amount(new BigDecimal("200.00")).build();

		testTicket = Ticket.builder().id(TICKET_ID).user(testUser).booking(testBooking).payment(testPayment)
				.ticketType(ticketType).uniqueCode(TICKET_CODE).originalPrice(new BigDecimal("200.00"))
				.finalPrice(new BigDecimal("200.00")).status(TicketStatus.ACTIVE).purchaseTime(LocalDateTime.now())
				.build();

		ticketResponse = TicketResponse.builder().id(TICKET_ID).ticketCode(TICKET_CODE).status(TicketStatus.ACTIVE)
				.price(new BigDecimal("200.00")).ticketType("Adult").movieTitle("Test Movie").hallName("Hall A").row(1)
				.seatNumber(1).build();
	}

	@Test
	void createTicketsForBooking_Success() {
		when(ticketRepository.saveAll(anyList())).thenReturn(Arrays.asList(testTicket));

		List<Ticket> tickets = ticketService.createTicketsForBooking(testBooking, testPayment);

		assertThat(tickets).hasSize(1);
		assertThat(tickets.get(0).getUniqueCode()).isNotNull();
		assertThat(tickets.get(0).getStatus()).isEqualTo(TicketStatus.ACTIVE);
		verify(ticketRepository).saveAll(anyList());
	}

	@Test
	void getTicketById_Success() {
		when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(testTicket));
		when(ticketMapper.toTicketResponse(testTicket)).thenReturn(ticketResponse);

		TicketResponse response = ticketService.getTicketById(TICKET_ID, testUser);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(TICKET_ID);
		assertThat(response.getTicketCode()).isEqualTo(TICKET_CODE);
		verify(ticketRepository).findById(TICKET_ID);
		verify(ticketMapper).toTicketResponse(testTicket);
	}

	@Test
	void getTicketById_WhenTicketNotFound_ShouldThrowException() {
		when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketService.getTicketById(TICKET_ID, testUser))
				.isInstanceOf(TicketValidationException.class);

		verify(ticketRepository).findById(TICKET_ID);
	}

	@Test
	void getTicketById_WhenAccessDenied_ShouldThrowException() {
		User otherUser = new User();
		otherUser.setId(999L);

		when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(testTicket));

		assertThatThrownBy(() -> ticketService.getTicketById(TICKET_ID, otherUser))
				.isInstanceOf(TicketValidationException.class);

		verify(ticketRepository).findById(TICKET_ID);
	}

	@Test
	void getUserTickets_WithStatusFilter() {
		List<Ticket> tickets = Arrays.asList(testTicket);
		when(ticketRepository.findByUserIdAndStatusOrderByPurchaseTimeDesc(USER_ID, TicketStatus.ACTIVE))
				.thenReturn(tickets);
		when(ticketMapper.toTicketResponse(testTicket)).thenReturn(ticketResponse);

		List<TicketResponse> responses = ticketService.getUserTickets(testUser, TicketStatus.ACTIVE);

		assertThat(responses).hasSize(1);
		assertThat(responses.get(0).getId()).isEqualTo(TICKET_ID);
		assertThat(responses.get(0).getTicketCode()).isEqualTo(TICKET_CODE);
		verify(ticketRepository).findByUserIdAndStatusOrderByPurchaseTimeDesc(USER_ID, TicketStatus.ACTIVE);
	}

	@Test
	void getUserTickets_WithoutStatusFilter() {
		List<Ticket> tickets = Arrays.asList(testTicket);
		when(ticketRepository.findByUserIdOrderByPurchaseTimeDesc(USER_ID)).thenReturn(tickets);
		when(ticketMapper.toTicketResponse(testTicket)).thenReturn(ticketResponse);

		List<TicketResponse> responses = ticketService.getUserTickets(testUser, null);

		assertThat(responses).hasSize(1);
		assertThat(responses.get(0).getId()).isEqualTo(TICKET_ID);
		assertThat(responses.get(0).getTicketCode()).isEqualTo(TICKET_CODE);
		verify(ticketRepository).findByUserIdOrderByPurchaseTimeDesc(USER_ID);
	}

	@Test
	void getBookingTickets_Success() {
		List<Ticket> tickets = Arrays.asList(testTicket);
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));
		when(ticketRepository.findByPaymentBookingId(BOOKING_ID)).thenReturn(tickets);
		when(ticketMapper.toTicketResponse(testTicket)).thenReturn(ticketResponse);

		List<TicketResponse> responses = ticketService.getBookingTickets(BOOKING_ID, testUser);

		assertThat(responses).hasSize(1);
		assertThat(responses.get(0).getId()).isEqualTo(TICKET_ID);
		assertThat(responses.get(0).getTicketCode()).isEqualTo(TICKET_CODE);
		verify(bookingRepository).findByIdAndUserId(BOOKING_ID, USER_ID);
		verify(ticketRepository).findByPaymentBookingId(BOOKING_ID);
	}

	@Test
	void getBookingTickets_WhenBookingNotFound_ShouldThrowException() {
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketService.getBookingTickets(BOOKING_ID, testUser))
				.isInstanceOf(BookingNotFoundException.class);

		verify(bookingRepository).findByIdAndUserId(BOOKING_ID, USER_ID);
	}

	@Test
	void validateTicket_Success() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));
		when(ticketRepository.save(testTicket)).thenReturn(testTicket);

		ticketService.validateTicket(TICKET_CODE);

		assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.USED);
		verify(ticketRepository).save(testTicket);
	}

	@Test
	void validateTicket_WhenTicketNotFound_ShouldThrowException() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketService.validateTicket(TICKET_CODE))
				.isInstanceOf(TicketValidationException.class);

		verify(ticketRepository).findByUniqueCode(TICKET_CODE);
	}

	@Test
	void validateTicket_WhenTicketAlreadyUsed_ShouldThrowException() {
		testTicket.setStatus(TicketStatus.USED);
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

		assertThatThrownBy(() -> ticketService.validateTicket(TICKET_CODE))
				.isInstanceOf(TicketValidationException.class).hasMessageContaining("already used");

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void validateTicket_WhenTicketCancelled_ShouldThrowException() {
		testTicket.setStatus(TicketStatus.CANCELLED);
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

		assertThatThrownBy(() -> ticketService.validateTicket(TICKET_CODE))
				.isInstanceOf(TicketValidationException.class).hasMessageContaining("cancelled");

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void validateTicket_WhenSessionStarted_ShouldThrowException() {
		testSession.setStartTime(LocalDateTime.now().minusMinutes(10));
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

		assertThatThrownBy(() -> ticketService.validateTicket(TICKET_CODE))
				.isInstanceOf(TicketValidationException.class).hasMessageContaining("already started");

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void validateTicket_WhenSessionCancelled_ShouldThrowException() {
		testSession.setStatus(CinemaSessionStatus.CANCELLED);
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

		assertThatThrownBy(() -> ticketService.validateTicket(TICKET_CODE))
				.isInstanceOf(TicketValidationException.class).hasMessageContaining("cancelled");

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void generateTicketQRCode_Success() {
		byte[] qrCode = new byte[] { 1, 2, 3 };
		when(qrCodeService.generateQRCode(anyString(), eq(200))).thenReturn(qrCode);

		byte[] result = ticketService.generateTicketQRCode(TICKET_CODE);

		assertThat(result).isEqualTo(qrCode);
		verify(qrCodeService).generateQRCode("http://localhost:8080/api/tickets/validate/" + TICKET_CODE, 200);
	}

	@Test
	void sendTicketsToUser_Success() {
		List<Ticket> tickets = Arrays.asList(testTicket);
		when(ticketRepository.findByPaymentBookingId(BOOKING_ID)).thenReturn(tickets);
		when(bookingService.generateBookingNumber(testBooking)).thenReturn("BK-2024-00002");

		doNothing().when(emailService).sendTicketsEmail(anyString(), anyString(), anyString(), anyString(), anyString(),
				any(), anyString(), anyString());

		ticketService.sendTicketsToUser(testBooking);

		verify(emailService).sendTicketsEmail(eq("test@example.com"), eq("BK-2024-00002"), eq("Test Movie"),
				anyString(), eq("Hall A"), any(), eq("Bank Card"), anyString());
	}

	@Test
	void sendTicketsToUser_WhenNoTickets_ShouldLogWarning() {
		when(ticketRepository.findByPaymentBookingId(BOOKING_ID)).thenReturn(Collections.emptyList());

		ticketService.sendTicketsToUser(testBooking);

		verify(emailService, never()).sendTicketsEmail(anyString(), anyString(), anyString(), anyString(), anyString(),
				any(), anyString(), anyString());
	}

	@Test
	void cancelTicketsForBooking_Success() {
		testTicket.setStatus(TicketStatus.ACTIVE);
		List<Ticket> tickets = Arrays.asList(testTicket);

		when(ticketRepository.findByPaymentBookingId(BOOKING_ID)).thenReturn(tickets);
		when(ticketRepository.saveAll(anyList())).thenReturn(tickets);

		ticketService.cancelTicketsForBooking(testBooking);

		assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
		verify(ticketRepository).saveAll(tickets);
	}

	@Test
	void cancelTicketsForBooking_WhenTicketsAlreadyCancelled() {
		testTicket.setStatus(TicketStatus.CANCELLED);
		List<Ticket> tickets = Arrays.asList(testTicket);

		when(ticketRepository.findByPaymentBookingId(BOOKING_ID)).thenReturn(tickets);

		ticketService.cancelTicketsForBooking(testBooking);

		assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
		verify(ticketRepository, never()).saveAll(anyList());
	}

	@Test
	void voidTicket_Success() {
		when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(testTicket));
		when(ticketRepository.save(testTicket)).thenReturn(testTicket);

		ticketService.voidTicket(TICKET_ID, testUser);

		assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
		verify(ticketRepository).save(testTicket);
	}

	@Test
	void voidTicket_WhenTicketNotFound_ShouldThrowException() {
		when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketService.voidTicket(TICKET_ID, testUser))
				.isInstanceOf(TicketValidationException.class);

		verify(ticketRepository).findById(TICKET_ID);
	}

	@Test
	void voidTicket_WhenAccessDenied_ShouldThrowException() {
		User otherUser = new User();
		otherUser.setId(999L);

		when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(testTicket));

		assertThatThrownBy(() -> ticketService.voidTicket(TICKET_ID, otherUser))
				.isInstanceOf(TicketValidationException.class);

		verify(ticketRepository).findById(TICKET_ID);
	}

	@Test
	void voidTicket_WhenTicketAlreadyUsed_ShouldThrowException() {
		testTicket.setStatus(TicketStatus.USED);
		when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(testTicket));

		assertThatThrownBy(() -> ticketService.voidTicket(TICKET_ID, testUser))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("Cannot void an already used ticket");

		verify(ticketRepository, never()).save(any());
	}

	@Test
	void voidTicket_WhenTicketAlreadyCancelled_ShouldDoNothing() {
		testTicket.setStatus(TicketStatus.CANCELLED);
		when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(testTicket));

		ticketService.voidTicket(TICKET_ID, testUser);

		assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
		verify(ticketRepository, never()).save(any());
	}

	@Test
	void isTicketValid_Success() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

		boolean result = ticketService.isTicketValid(TICKET_CODE);

		assertThat(result).isTrue();
		verify(ticketRepository).findByUniqueCode(TICKET_CODE);
	}

	@Test
	void isTicketValid_WhenTicketNotFound() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

		boolean result = ticketService.isTicketValid(TICKET_CODE);

		assertThat(result).isFalse();
		verify(ticketRepository).findByUniqueCode(TICKET_CODE);
	}

	@Test
	void isTicketValid_WhenTicketInvalid() {
		testTicket.setStatus(TicketStatus.USED);
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

		boolean result = ticketService.isTicketValid(TICKET_CODE);

		assertThat(result).isFalse();
		verify(ticketRepository).findByUniqueCode(TICKET_CODE);
	}

	@Test
	void checkTicketStatus_Success() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

		TicketStatus result = ticketService.checkTicketStatus(TICKET_CODE);

		assertThat(result).isEqualTo(TicketStatus.ACTIVE);
		verify(ticketRepository).findByUniqueCode(TICKET_CODE);
	}

	@Test
	void checkTicketStatus_WhenTicketNotFound() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

		TicketStatus result = ticketService.checkTicketStatus(TICKET_CODE);

		assertThat(result).isNull();
		verify(ticketRepository).findByUniqueCode(TICKET_CODE);
	}

	@Test
	void calculateTicketPrice_Success() {
		BigDecimal result = invokePrivateCalculateTicketPrice(bookedSeat);

		BigDecimal expected = BASE_PRICE.multiply(seat.getSeatType().getPriceMultiplier())
				.multiply(ticketType.getPriceMultiplier());

		assertThat(result).isEqualByComparingTo(expected);
	}

	@Test
	void generateTicketCode_ShouldGenerateUniqueCode() {
		String code1 = invokePrivateGenerateTicketCode();
		String code2 = invokePrivateGenerateTicketCode();

		assertThat(code1).isNotEqualTo(code2);
		assertThat(code1).startsWith("TKT-");
		assertThat(code1).hasSize(16);
	}

	@Test
	void generateQrCodeUrl_Success() {
		String result = invokePrivateGenerateQrCodeUrl(TICKET_CODE);
		assertThat(result).isEqualTo("http://localhost:8080/api/tickets/" + TICKET_CODE + "/qr");
	}

	@Test
	void validateTicketForEntry_Success() {
		invokePrivateValidateTicketForEntry(testTicket);
	}

	@Test
	void isTicketValidForEntry_Success() {
		boolean result = invokePrivateIsTicketValidForEntry(testTicket);
		assertThat(result).isTrue();
	}

	private BigDecimal invokePrivateCalculateTicketPrice(BookedSeat bookedSeat) {
		try {
			var method = TicketService.class.getDeclaredMethod("calculateTicketPrice", BookedSeat.class);
			method.setAccessible(true);
			return (BigDecimal) method.invoke(ticketService, bookedSeat);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String invokePrivateGenerateTicketCode() {
		try {
			var method = TicketService.class.getDeclaredMethod("generateTicketCode");
			method.setAccessible(true);
			return (String) method.invoke(ticketService);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String invokePrivateGenerateQrCodeUrl(String ticketCode) {
		try {
			var method = TicketService.class.getDeclaredMethod("generateQrCodeUrl", String.class);
			method.setAccessible(true);
			return (String) method.invoke(ticketService, ticketCode);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void invokePrivateValidateTicketForEntry(Ticket ticket) {
		try {
			var method = TicketService.class.getDeclaredMethod("validateTicketForEntry", Ticket.class);
			method.setAccessible(true);
			method.invoke(ticketService, ticket);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean invokePrivateIsTicketValidForEntry(Ticket ticket) {
		try {
			var method = TicketService.class.getDeclaredMethod("isTicketValidForEntry", Ticket.class);
			method.setAccessible(true);
			return (boolean) method.invoke(ticketService, ticket);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}