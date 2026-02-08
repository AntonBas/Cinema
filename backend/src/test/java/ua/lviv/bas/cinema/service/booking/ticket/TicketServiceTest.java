package ua.lviv.bas.cinema.service.booking.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import ua.lviv.bas.cinema.domain.SeatReservation;
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
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketValidationException;
import ua.lviv.bas.cinema.repository.TicketRepository;
import ua.lviv.bas.cinema.service.integration.qr.QRCodeService;
import ua.lviv.bas.cinema.service.notification.EmailService;
import ua.lviv.bas.cinema.service.shared.NumberGeneratorService;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private TicketValidationService validationService;

	@Mock
	private QRCodeService qrCodeService;

	@Mock
	private EmailService emailService;

	@Mock
	private NumberGeneratorService numberGenerator;

	@InjectMocks
	private TicketService ticketService;

	private User testUser;
	private Booking testBooking;
	private Payment testPayment;
	private Session testSession;
	private SeatReservation bookedSeat;
	private TicketType ticketType;
	private Ticket testTicket;

	private static final Long USER_ID = 1L;
	private static final Long BOOKING_ID = 2L;
	private static final String TICKET_CODE = "TKT-123456";

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

		Seat seat = new Seat();
		seat.setRow(1);
		seat.setNumber(1);

		bookedSeat = new SeatReservation();
		bookedSeat.setTicketType(ticketType);
		bookedSeat.setSeat(seat);
		bookedSeat.setSeatPrice(new BigDecimal("200.00"));

		testBooking = new Booking();
		testBooking.setId(BOOKING_ID);
		testBooking.setUser(testUser);
		testBooking.setSession(testSession);
		testBooking.setStatus(BookingStatus.CONFIRMED);
		testBooking.setFinalPrice(new BigDecimal("200.00"));
		testBooking.setBookedSeats(Arrays.asList(bookedSeat));

		testPayment = new Payment();
		testPayment.setId(1L);
		testPayment.setBooking(testBooking);

		testTicket = new Ticket();
		testTicket.setId(1L);
		testTicket.setUser(testUser);
		testTicket.setBooking(testBooking);
		testTicket.setPayment(testPayment);
		testTicket.setTicketType(ticketType);
		testTicket.setUniqueCode(TICKET_CODE);
		testTicket.setOriginalPrice(new BigDecimal("200.00"));
		testTicket.setFinalPrice(new BigDecimal("200.00"));
		testTicket.setStatus(TicketStatus.ACTIVE);
		testTicket.setPurchaseTime(LocalDateTime.now());
	}

	@Test
	void createTicketsForBooking_Success() {
		when(numberGenerator.generateTicketCode()).thenReturn("TKT-NEW-123");
		when(ticketRepository.saveAll(anyList())).thenReturn(Arrays.asList(testTicket));

		List<Ticket> tickets = ticketService.createTicketsForBooking(testBooking, testPayment);

		assertThat(tickets).isNotEmpty();
		verify(ticketRepository).saveAll(anyList());
	}

	@Test
	void validateTicket_Success() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));
		doNothing().when(validationService).validateTicketForEntry(testTicket);
		when(ticketRepository.save(testTicket)).thenReturn(testTicket);

		ticketService.validateTicket(TICKET_CODE);

		assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.USED);
		verify(validationService).validateTicketForEntry(testTicket);
	}

	@Test
	void validateTicket_WhenTicketNotFound_ShouldThrowException() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketService.validateTicket(TICKET_CODE))
				.isInstanceOf(TicketValidationException.class);
	}

	@Test
	void generateTicketQRCode_Success() {
		byte[] expectedQrCode = new byte[] { 1, 2, 3 };
		when(qrCodeService.generateQRCode(anyString(), eq(200))).thenReturn(expectedQrCode);

		byte[] result = ticketService.generateTicketQRCode(TICKET_CODE);

		assertThat(result).isEqualTo(expectedQrCode);
		verify(qrCodeService).generateQRCode("http://localhost:8080/api/tickets/validate/" + TICKET_CODE, 200);
	}

	@Test
	void isTicketValid_Success() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));
		when(validationService.isTicketValidForEntry(testTicket)).thenReturn(true);

		boolean result = ticketService.isTicketValid(TICKET_CODE);

		assertThat(result).isTrue();
	}

	@Test
	void isTicketValid_WhenTicketNotFound() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

		boolean result = ticketService.isTicketValid(TICKET_CODE);

		assertThat(result).isFalse();
	}

	@Test
	void checkTicketStatus_Success() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));

		TicketStatus result = ticketService.checkTicketStatus(TICKET_CODE);

		assertThat(result).isEqualTo(TicketStatus.ACTIVE);
	}

	@Test
	void checkTicketStatus_WhenTicketNotFound() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

		TicketStatus result = ticketService.checkTicketStatus(TICKET_CODE);

		assertThat(result).isNull();
	}
}