package ua.lviv.bas.cinema.service.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.booking.status.BookingStatus;
import ua.lviv.bas.cinema.domain.booking.status.PaymentStatus;
import ua.lviv.bas.cinema.domain.booking.status.ReservationStatus;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.payment.request.PaymentCreateRequest;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.payment.InvalidPaymentStatusException;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentAccessDeniedException;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentProcessingException;
import ua.lviv.bas.cinema.repository.booking.BookingRepository;
import ua.lviv.bas.cinema.repository.booking.PaymentRepository;
import ua.lviv.bas.cinema.service.bonus.BonusService;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;
import ua.lviv.bas.cinema.service.integration.payment.PaymentGatewayService;
import ua.lviv.bas.cinema.service.notification.EmailService;
import ua.lviv.bas.cinema.service.shared.DateTimeFormatterService;
import ua.lviv.bas.cinema.service.shared.NumberGeneratorService;
import ua.lviv.bas.cinema.service.ticket.TicketService;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

	@Mock
	private PaymentRepository paymentRepository;
	@Mock
	private BookingRepository bookingRepository;
	@Mock
	private PaymentGatewayService paymentGatewayService;
	@Mock
	private TicketService ticketService;
	@Mock
	private BonusService bonusService;
	@Mock
	private NumberGeneratorService numberGenerator;
	@Mock
	private BookingService bookingService;
	@Mock
	private AuditService auditService;
	@Mock
	private EmailService emailService;
	@Mock
	private DateTimeFormatterService dateTimeFormatter;

	@InjectMocks
	private PaymentService paymentService;

	private User testUser;
	private Booking testBooking;
	private Payment testPayment;
	private PaymentCreateRequest createRequest;

	private static final Long USER_ID = 1L;
	private static final Long BOOKING_ID = 2L;
	private static final Long PAYMENT_ID = 3L;
	private static final BigDecimal AMOUNT = new BigDecimal("200.00");
	private static final int SESSION_TOO_CLOSE_MINUTES = 30;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(paymentService, "sessionTooCloseMinutes", SESSION_TOO_CLOSE_MINUTES);

		testUser = User.builder().id(USER_ID).email("test@example.com").build();

		Movie movie = Movie.builder().title("Test Movie").build();
		CinemaHall hall = CinemaHall.builder().name("Hall A").build();

		Session session = Session.builder().movie(movie).hall(hall).startTime(LocalDateTime.now().plusHours(2)).build();

		SeatReservation seatReservation = SeatReservation.builder().seat(Seat.builder().row(1).number(1).build())
				.status(ReservationStatus.CONFIRMED).build();

		testBooking = Booking.builder().id(BOOKING_ID).user(testUser).session(session).status(BookingStatus.PENDING)
				.finalPrice(AMOUNT).expiresAt(LocalDateTime.now().plusHours(1))
				.seatReservations(Arrays.asList(seatReservation)).build();

		testPayment = Payment.builder().id(PAYMENT_ID).booking(testBooking).amount(AMOUNT).status(PaymentStatus.PENDING)
				.liqpayOrderId("ORD_TEST123456789").build();

		createRequest = new PaymentCreateRequest(BOOKING_ID);
	}

	@Test
	void createPayment_Success() {
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));
		when(paymentRepository.findByBookingId(BOOKING_ID)).thenReturn(Optional.empty());
		when(numberGenerator.generateLiqpayOrderId()).thenReturn("ORD_NEW123456789");
		when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
		when(numberGenerator.generateBookingNumber(testBooking)).thenReturn("BK-2024-00001");

		PaymentResponse response = paymentService.createPayment(createRequest, testUser);

		assertThat(response).isNotNull();
		verify(paymentRepository).save(any(Payment.class));
	}

	@Test
	void createPayment_WhenBookingNotFound_ShouldThrowException() {
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> paymentService.createPayment(createRequest, testUser))
				.isInstanceOf(BookingNotFoundException.class);
	}

	@Test
	void createPayment_WhenPaymentInProgress_ShouldThrowException() {
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));
		when(paymentRepository.findByBookingId(BOOKING_ID)).thenReturn(Optional.of(testPayment));

		assertThatThrownBy(() -> paymentService.createPayment(createRequest, testUser))
				.isInstanceOf(PaymentProcessingException.class);
	}

	@Test
	void getPaymentStatus_Success() {
		when(paymentRepository.findByIdWithDetails(PAYMENT_ID)).thenReturn(Optional.of(testPayment));
		when(numberGenerator.generateBookingNumber(testBooking)).thenReturn("BK-2024-00001");

		PaymentResponse response = paymentService.getPaymentStatus(PAYMENT_ID, testUser);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(PAYMENT_ID);
	}

	@Test
	void getPaymentStatus_WhenPaymentNotFound_ShouldThrowException() {
		when(paymentRepository.findByIdWithDetails(PAYMENT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> paymentService.getPaymentStatus(PAYMENT_ID, testUser))
				.isInstanceOf(PaymentNotFoundException.class);
	}

	@Test
	void getPaymentStatus_WhenUserNotAuthorized_ShouldThrowException() {
		User otherUser = User.builder().id(999L).build();

		when(paymentRepository.findByIdWithDetails(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

		assertThatThrownBy(() -> paymentService.getPaymentStatus(PAYMENT_ID, otherUser))
				.isInstanceOf(PaymentAccessDeniedException.class);
	}

	@Test
	void retryPayment_Success() {
		testPayment.setStatus(PaymentStatus.FAILED);

		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(testPayment));
		when(numberGenerator.generateLiqpayOrderId()).thenReturn("ORD_NEW789");
		when(paymentRepository.save(testPayment)).thenReturn(testPayment);
		when(numberGenerator.generateBookingNumber(testBooking)).thenReturn("BK-2024-00001");

		PaymentResponse response = paymentService.retryPayment(PAYMENT_ID, testUser);

		assertThat(response).isNotNull();
		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
	}

	@Test
	void retryPayment_WhenPaymentNotFound_ShouldThrowException() {
		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> paymentService.retryPayment(PAYMENT_ID, testUser))
				.isInstanceOf(PaymentNotFoundException.class);
	}

	@Test
	void retryPayment_WhenPaymentNotFailed_ShouldThrowException() {
		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

		assertThatThrownBy(() -> paymentService.retryPayment(PAYMENT_ID, testUser))
				.isInstanceOf(InvalidPaymentStatusException.class);
	}

	@Test
	void processSuccessfulPayment_Success() {
		Map<String, String> callbackData = new HashMap<>();
		callbackData.put("payment_id", "PAY123");

		List<Ticket> tickets = Arrays.asList(Ticket.builder().build());

		when(ticketService.createTicketsForBooking(testBooking, testPayment)).thenReturn(tickets);
		when(bonusService.calculatePoints(AMOUNT)).thenReturn(20);
		when(dateTimeFormatter.formatStandard(any(LocalDateTime.class))).thenReturn("2024-01-01 14:00");
		when(numberGenerator.generateBookingNumber(testBooking)).thenReturn("BK-2024-00001");

		paymentService.processSuccessfulPayment(testPayment, callbackData);

		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
		verify(bookingService).confirmBooking(BOOKING_ID);
		verify(ticketService).createTicketsForBooking(testBooking, testPayment);
		verify(bonusService).accruePoints(USER_ID, 20, testBooking, testPayment);
	}

	@Test
	void processFailedPayment_Success() {
		Map<String, String> callbackData = new HashMap<>();
		callbackData.put("err_description", "Insufficient funds");

		paymentService.processFailedPayment(testPayment, callbackData);

		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
	}

	@Test
	void refundPayment_Success() {
		testPayment.setStatus(PaymentStatus.SUCCESS);
		testPayment.setLiqpayPaymentId("PAY123");
		BigDecimal refundAmount = new BigDecimal("100.00");
		String description = "Test refund";

		when(paymentGatewayService.prepareRefundData("PAY123", testPayment.getLiqpayOrderId(), refundAmount,
				description)).thenReturn("refund_data");
		when(paymentRepository.save(testPayment)).thenReturn(testPayment);

		paymentService.refundPayment(testPayment, refundAmount, description);

		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.PARTIALLY_REFUNDED);
		verify(paymentGatewayService).processRefund("refund_data");
	}

	@Test
	void refundPayment_WhenPaymentNotSuccess_ShouldThrowException() {
		testPayment.setStatus(PaymentStatus.PENDING);
		BigDecimal refundAmount = new BigDecimal("100.00");

		assertThatThrownBy(() -> paymentService.refundPayment(testPayment, refundAmount, "Test"))
				.isInstanceOf(PaymentProcessingException.class);
	}

	@Test
	void refundPayment_WhenFullRefund_ShouldMarkAsFullyRefunded() {
		testPayment.setStatus(PaymentStatus.SUCCESS);
		testPayment.setLiqpayPaymentId("PAY123");
		BigDecimal refundAmount = AMOUNT;
		String description = "Full refund";

		when(paymentGatewayService.prepareRefundData("PAY123", testPayment.getLiqpayOrderId(), refundAmount,
				description)).thenReturn("refund_data");
		when(paymentRepository.save(testPayment)).thenReturn(testPayment);

		paymentService.refundPayment(testPayment, refundAmount, description);

		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
	}
}