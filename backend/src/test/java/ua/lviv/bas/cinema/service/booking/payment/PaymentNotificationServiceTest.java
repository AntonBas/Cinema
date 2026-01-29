package ua.lviv.bas.cinema.service.booking.payment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
		testUser = User.builder().id(1L).email("test@example.com").build();

		Movie movie = Movie.builder().title("Test Movie").build();

		CinemaHall hall = CinemaHall.builder().name("Hall A").build();

		testSession = Session.builder().id(1L).movie(movie).hall(hall).startTime(LocalDateTime.now().plusHours(2))
				.build();

		Seat seat1 = Seat.builder().row(1).number(1).build();

		Seat seat2 = Seat.builder().row(1).number(2).build();

		BookedSeat bookedSeat1 = BookedSeat.builder().seat(seat1).build();

		BookedSeat bookedSeat2 = BookedSeat.builder().seat(seat2).build();

		testBooking = Booking.builder().id(1L).user(testUser).session(testSession)
				.bookedSeats(Arrays.asList(bookedSeat1, bookedSeat2)).build();

		testPayment = Payment.builder().id(1L).booking(testBooking).amount(new BigDecimal("200.00")).build();

		testTickets = Arrays.asList(Ticket.builder().build(), Ticket.builder().build());

		when(dateTimeFormatter.formatStandard(any(LocalDateTime.class))).thenReturn("2024-01-01 14:00");
		when(numberGenerator.generateBookingNumber(testBooking)).thenReturn("BK-2024-00001");
	}

	@Test
	void sendPaymentSuccessEmail_ShouldExtractDataAndCallEmailService() {
		paymentNotificationService.sendPaymentSuccessEmail(testPayment, testBooking, testTickets);

		verify(emailService).sendTicketsEmail(eq("test@example.com"), eq("BK-2024-00001"), eq("Test Movie"),
				eq("2024-01-01 14:00"), eq("Hall A"), eq(new BigDecimal("200.00")), eq("Credit card"),
				eq("Row 1, Seat 1, Row 1, Seat 2"));
	}

	@Test
	void sendPaymentFailedEmail_WithErrorDescription_ShouldCallEmailService() {
		testPayment.setLiqpayErrorDescription("Insufficient funds");

		paymentNotificationService.sendPaymentFailedEmail(testPayment, testBooking);

		verify(emailService).sendPaymentFailedEmail(eq("test@example.com"), eq("BK-2024-00001"), eq("Test Movie"),
				eq("2024-01-01 14:00"), eq("Insufficient funds"));
	}

	@Test
	void sendPaymentFailedEmail_WithoutErrorDescription_ShouldUseDefaultMessage() {
		testPayment.setLiqpayErrorDescription(null);

		paymentNotificationService.sendPaymentFailedEmail(testPayment, testBooking);

		verify(emailService).sendPaymentFailedEmail(eq("test@example.com"), eq("BK-2024-00001"), eq("Test Movie"),
				eq("2024-01-01 14:00"), eq("Payment error"));
	}

	@Test
	void sendRefundEmail_ShouldExtractDataAndCallEmailService() {
		BigDecimal refundAmount = new BigDecimal("100.00");
		String description = "Cancellation";

		paymentNotificationService.sendRefundEmail(testPayment, refundAmount, description);

		verify(emailService).sendRefundEmail(eq("test@example.com"), eq("BK-2024-00001"), eq("Test Movie"),
				eq("2024-01-01 14:00"), eq("Hall A"), eq(refundAmount), eq("Row 1, Seat 1, Row 1, Seat 2"),
				eq(description));
	}

	@Test
	void sendPaymentSuccessEmail_WhenEmailServiceThrowsException_ShouldNotPropagate() {
		doThrow(new RuntimeException("Email service error")).when(emailService).sendTicketsEmail(anyString(),
				anyString(), anyString(), anyString(), anyString(), any(), anyString(), anyString());

		assertDoesNotThrow(
				() -> paymentNotificationService.sendPaymentSuccessEmail(testPayment, testBooking, testTickets));
	}

	@Test
	void sendPaymentFailedEmail_WhenEmailServiceThrowsException_ShouldNotPropagate() {
		doThrow(new RuntimeException("Email service error")).when(emailService).sendPaymentFailedEmail(anyString(),
				anyString(), anyString(), anyString(), anyString());

		assertDoesNotThrow(() -> paymentNotificationService.sendPaymentFailedEmail(testPayment, testBooking));
	}

	@Test
	void sendRefundEmail_WhenEmailServiceThrowsException_ShouldNotPropagate() {
		BigDecimal refundAmount = new BigDecimal("100.00");
		String description = "Cancellation";

		doThrow(new RuntimeException("Email service error")).when(emailService).sendRefundEmail(anyString(),
				anyString(), anyString(), anyString(), anyString(), any(), anyString(), anyString());

		assertDoesNotThrow(() -> paymentNotificationService.sendRefundEmail(testPayment, refundAmount, description));
	}

	@Test
	void sendPaymentSuccessEmail_ShouldFormatSeatsInfoCorrectly() {
		Seat seat3 = Seat.builder().row(2).number(3).build();
		Seat seat4 = Seat.builder().row(2).number(4).build();
		BookedSeat bookedSeat3 = BookedSeat.builder().seat(seat3).build();
		BookedSeat bookedSeat4 = BookedSeat.builder().seat(seat4).build();

		testBooking.setBookedSeats(Arrays.asList(bookedSeat3, bookedSeat4));

		paymentNotificationService.sendPaymentSuccessEmail(testPayment, testBooking, testTickets);

		verify(emailService).sendTicketsEmail(anyString(), anyString(), anyString(), anyString(), anyString(), any(),
				anyString(), eq("Row 2, Seat 3, Row 2, Seat 4"));
	}

	@Test
	void sendPaymentSuccessEmail_ShouldHandleSingleSeat() {
		Seat singleSeat = Seat.builder().row(3).number(5).build();
		BookedSeat bookedSeat = BookedSeat.builder().seat(singleSeat).build();
		testBooking.setBookedSeats(Arrays.asList(bookedSeat));

		paymentNotificationService.sendPaymentSuccessEmail(testPayment, testBooking, testTickets);

		verify(emailService).sendTicketsEmail(anyString(), anyString(), anyString(), anyString(), anyString(), any(),
				anyString(), eq("Row 3, Seat 5"));
	}
}