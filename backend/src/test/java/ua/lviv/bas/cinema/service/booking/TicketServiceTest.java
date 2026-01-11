package ua.lviv.bas.cinema.service.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
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
import ua.lviv.bas.cinema.exception.domain.ticket.TicketOperationException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketValidationException;
import ua.lviv.bas.cinema.mapper.TicketMapper;
import ua.lviv.bas.cinema.repository.BookingRepository;
import ua.lviv.bas.cinema.repository.TicketRepository;
import ua.lviv.bas.cinema.service.infrastructure.QRCodeService;
import ua.lviv.bas.cinema.service.notification.EmailService;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private TicketMapper ticketMapper;

	@Mock
	private EmailService emailService;

	@Mock
	private QRCodeService qrCodeService;

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
	private final Long TICKET_ID = 4L;
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
		testSession.setMovie(movie);
		testSession.setHall(hall);
		testSession.setStartTime(LocalDateTime.now().plusHours(2));
		testSession.setStatus(CinemaSessionStatus.SCHEDULED);

		ticketType = new TicketType();
		ticketType.setId(1L);
		ticketType.setDisplayName("Adult");
		ticketType.setPriceMultiplier(new BigDecimal("1.0"));

		seat = new Seat();
		seat.setRow(1);
		seat.setNumber(1);
		seat.setSeatType(SeatType.STANDARD);

		bookedSeat = new BookedSeat();
		bookedSeat.setTicketType(ticketType);
		bookedSeat.setSeat(seat);
		bookedSeat.setSeatPrice(new BigDecimal("200.00"));

		testBooking = Booking.builder().id(BOOKING_ID).user(testUser).session(testSession)
				.status(BookingStatus.CONFIRMED).bookedSeats(Arrays.asList(bookedSeat)).createdAt(LocalDateTime.now())
				.build();

		testPayment = Payment.builder().booking(testBooking).amount(new BigDecimal("200.00")).build();

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
		List<Ticket> tickets = Arrays.asList(testTicket);
		when(ticketRepository.saveAll(anyList())).thenReturn(tickets);

		List<Ticket> result = ticketService.createTicketsForBooking(testBooking, testPayment);

		assertThat(result).hasSize(1);
		verify(ticketRepository).saveAll(anyList());
	}

	@Test
	void getTicketById_Success() {
		when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(testTicket));
		when(ticketMapper.toTicketResponse(testTicket)).thenReturn(ticketResponse);

		TicketResponse result = ticketService.getTicketById(TICKET_ID, testUser);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(TICKET_ID);
	}

	@Test
	void getTicketById_WhenTicketNotFound_ShouldThrowException() {
		when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketService.getTicketById(TICKET_ID, testUser))
				.isInstanceOf(TicketValidationException.class);
	}

	@Test
	void getTicketById_WhenAccessDenied_ShouldThrowException() {
		User otherUser = new User();
		otherUser.setId(999L);

		when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(testTicket));

		assertThatThrownBy(() -> ticketService.getTicketById(TICKET_ID, otherUser))
				.isInstanceOf(TicketValidationException.class);
	}

	@Test
	void getUserTickets_WithStatusFilter() {
		List<Ticket> tickets = Arrays.asList(testTicket);
		when(ticketRepository.findByUserIdAndStatusOrderByPurchaseTimeDesc(USER_ID, TicketStatus.ACTIVE))
				.thenReturn(tickets);
		when(ticketMapper.toTicketResponse(testTicket)).thenReturn(ticketResponse);

		List<TicketResponse> result = ticketService.getUserTickets(testUser, TicketStatus.ACTIVE);

		assertThat(result).hasSize(1);
	}

	@Test
	void getUserTickets_WithoutStatusFilter() {
		List<Ticket> tickets = Arrays.asList(testTicket);
		when(ticketRepository.findByUserIdOrderByPurchaseTimeDesc(USER_ID)).thenReturn(tickets);
		when(ticketMapper.toTicketResponse(testTicket)).thenReturn(ticketResponse);

		List<TicketResponse> result = ticketService.getUserTickets(testUser, null);

		assertThat(result).hasSize(1);
	}

	@Test
	void getBookingTickets_Success() {
		List<Ticket> tickets = Arrays.asList(testTicket);
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));
		when(ticketRepository.findByBookingId(BOOKING_ID)).thenReturn(tickets);
		when(ticketMapper.toTicketResponse(testTicket)).thenReturn(ticketResponse);

		List<TicketResponse> result = ticketService.getBookingTickets(BOOKING_ID, testUser);

		assertThat(result).hasSize(1);
	}

	@Test
	void getBookingTickets_WhenBookingNotFound_ShouldThrowException() {
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketService.getBookingTickets(BOOKING_ID, testUser))
				.isInstanceOf(BookingNotFoundException.class);
	}

	@Test
	void validateTicket_Success() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));
		when(ticketRepository.save(testTicket)).thenReturn(testTicket);

		ticketService.validateTicket(TICKET_CODE);

		assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.USED);
	}

	@Test
	void validateTicket_WhenTicketNotFound_ShouldThrowException() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketService.validateTicket(TICKET_CODE))
				.isInstanceOf(TicketValidationException.class);
	}

	@Test
	void validateTicket_WhenTicketAlreadyUsed_ShouldThrowException() {
		testTicket.setStatus(TicketStatus.USED);
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

		assertThatThrownBy(() -> ticketService.validateTicket(TICKET_CODE))
				.isInstanceOf(TicketValidationException.class);
	}

	@Test
	void generateTicketQRCode_Success() {
		byte[] qrCode = new byte[] { 1, 2, 3 };
		when(qrCodeService.generateQRCode(anyString(), eq(200))).thenReturn(qrCode);

		byte[] result = ticketService.generateTicketQRCode(TICKET_CODE);

		assertThat(result).isEqualTo(qrCode);
	}

	@Test
	void sendTicketsToUser_Success() {
		List<Ticket> tickets = Arrays.asList(testTicket);
		when(ticketRepository.findByBookingId(BOOKING_ID)).thenReturn(tickets);
		doNothing().when(emailService).sendTicketsEmail(anyString(), anyString(), anyString(), anyString(), anyString(),
				any(), anyString(), anyString());

		ticketService.sendTicketsToUser(testBooking);

		verify(emailService).sendTicketsEmail(eq("test@example.com"), anyString(), eq("Test Movie"), anyString(),
				eq("Hall A"), any(), eq("Credit Card"), anyString());
	}

	@Test
	void cancelTicketsForBooking_Success() {
		testTicket.setStatus(TicketStatus.ACTIVE);
		List<Ticket> tickets = Arrays.asList(testTicket);

		when(ticketRepository.findByBookingId(BOOKING_ID)).thenReturn(tickets);
		when(ticketRepository.saveAll(anyList())).thenReturn(tickets);

		ticketService.cancelTicketsForBooking(testBooking);

		assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
	}

	@Test
	void voidTicket_Success() {
		when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(testTicket));
		when(ticketRepository.save(testTicket)).thenReturn(testTicket);

		ticketService.voidTicket(TICKET_ID, testUser);

		assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
	}

	@Test
	void voidTicket_WhenTicketNotFound_ShouldThrowException() {
		when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketService.voidTicket(TICKET_ID, testUser))
				.isInstanceOf(TicketValidationException.class);
	}

	@Test
	void voidTicket_WhenAccessDenied_ShouldThrowException() {
		User otherUser = new User();
		otherUser.setId(999L);

		when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(testTicket));

		assertThatThrownBy(() -> ticketService.voidTicket(TICKET_ID, otherUser))
				.isInstanceOf(TicketValidationException.class);
	}

	@Test
	void voidTicket_WhenTicketAlreadyUsed_ShouldThrowException() {
		testTicket.setStatus(TicketStatus.USED);
		when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(testTicket));

		assertThatThrownBy(() -> ticketService.voidTicket(TICKET_ID, testUser))
				.isInstanceOf(TicketOperationException.class);
	}

	@Test
	void isTicketValid_Success() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

		boolean result = ticketService.isTicketValid(TICKET_CODE);

		assertThat(result).isTrue();
	}

	@Test
	void checkTicketStatus_Success() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

		TicketStatus result = ticketService.checkTicketStatus(TICKET_CODE);

		assertThat(result).isEqualTo(TicketStatus.ACTIVE);
	}
}