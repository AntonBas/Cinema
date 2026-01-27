package ua.lviv.bas.cinema.service.booking.payment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.BookedSeat;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.service.notification.EmailService;
import ua.lviv.bas.cinema.service.shared.DateTimeFormatterService;
import ua.lviv.bas.cinema.service.shared.NumberGeneratorService;

@ExtendWith(MockitoExtension.class)
public class PaymentNotificationServiceTest {

	@Mock
	private EmailService emailService;

	@Mock
	private DateTimeFormatterService dateTimeFormatter;

	@Mock
	private NumberGeneratorService numberGenerator;

	@InjectMocks
	private PaymentNotificationService paymentNotificationService;

	private User testUser;
	private Session testSession;
	private Booking testBooking;
	private Payment testPayment;
	private List<Ticket> testTickets;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setId(1L);
		testUser.setEmail("test@example.com");

		Movie movie = new Movie();
		movie.setTitle("Test Movie");

		CinemaHall hall = new CinemaHall();
		hall.setName("Hall A");

		testSession = new Session();
		testSession.setId(1L);
		testSession.setMovie(movie);
		testSession.setHall(hall);
		testSession.setStartTime(LocalDateTime.now().plusHours(2));

		Seat seat1 = new Seat();
		seat1.setRow(1);
		seat1.setNumber(1);

		Seat seat2 = new Seat();
		seat2.setRow(1);
		seat2.setNumber(2);

		BookedSeat bookedSeat1 = new BookedSeat();
		bookedSeat1.setSeat(seat1);

		BookedSeat bookedSeat2 = new BookedSeat();
		bookedSeat2.setSeat(seat2);

		testBooking = new Booking();
		testBooking.setId(1L);
		testBooking.setUser(testUser);
		testBooking.setSession(testSession);
		testBooking.setBookedSeats(Arrays.asList(bookedSeat1, bookedSeat2));

		testPayment = new Payment();
		testPayment.setId(1L);
		testPayment.setBooking(testBooking);
		testPayment.setAmount(new BigDecimal("200.00"));

		testTickets = Arrays.asList(new Ticket(), new Ticket());

		org.mockito.Mockito.when(dateTimeFormatter.formatStandard(any(LocalDateTime.class)))
				.thenReturn("2024-01-01 14:00");
		org.mockito.Mockito.when(numberGenerator.generateBookingNumber(testBooking)).thenReturn("BK-2024-00001");
	}

	@Test
	void sendPaymentSuccessEmail_Success() {
		paymentNotificationService.sendPaymentSuccessEmail(testPayment, testBooking, testTickets);

		verify(emailService).sendTicketsEmail("test@example.com", "BK-2024-00001", "Test Movie", "2024-01-01 14:00",
				"Hall A", new BigDecimal("200.00"), "Credit card", "Row 1, Seat 1, Row 1, Seat 2");
	}

	@Test
	void sendPaymentFailedEmail_Success() {
		testPayment.setLiqpayErrorDescription("Insufficient funds");

		paymentNotificationService.sendPaymentFailedEmail(testPayment, testBooking);

		verify(emailService).sendPaymentFailedEmail("test@example.com", "BK-2024-00001", "Test Movie",
				"2024-01-01 14:00", "Insufficient funds");
	}

	@Test
	void sendRefundEmail_Success() {
		BigDecimal refundAmount = new BigDecimal("100.00");
		String description = "Cancellation";

		paymentNotificationService.sendRefundEmail(testPayment, refundAmount, description);

		verify(emailService).sendRefundEmail("test@example.com", "BK-2024-00001", "Test Movie", "2024-01-01 14:00",
				"Hall A", refundAmount, "Row 1, Seat 1, Row 1, Seat 2", description);
	}

	@Test
	void sendPaymentSuccessEmail_WhenException_ShouldLogError() {
		doThrow(new RuntimeException("Email service error")).when(emailService).sendTicketsEmail(anyString(),
				anyString(), anyString(), anyString(), anyString(), any(), anyString(), anyString());

		paymentNotificationService.sendPaymentSuccessEmail(testPayment, testBooking, testTickets);
	}

	@Test
	void sendPaymentFailedEmail_WhenException_ShouldLogError() {
		doThrow(new RuntimeException("Email service error")).when(emailService).sendPaymentFailedEmail(anyString(),
				anyString(), anyString(), anyString(), anyString());

		paymentNotificationService.sendPaymentFailedEmail(testPayment, testBooking);
	}
}