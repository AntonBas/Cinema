package ua.lviv.bas.cinema.service.booking.payment;

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

import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;
import ua.lviv.bas.cinema.dto.payment.request.PaymentCreateRequest;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.exception.domain.payment.InvalidPaymentStatusException;
import ua.lviv.bas.cinema.exception.domain.payment.PaymentAccessDeniedException;
import ua.lviv.bas.cinema.exception.domain.payment.PaymentNotFoundException;
import ua.lviv.bas.cinema.exception.domain.payment.PaymentProcessingException;
import ua.lviv.bas.cinema.repository.BookingRepository;
import ua.lviv.bas.cinema.repository.PaymentRepository;
import ua.lviv.bas.cinema.service.booking.management.BookingManagementService;
import ua.lviv.bas.cinema.service.booking.ticket.TicketService;
import ua.lviv.bas.cinema.service.integration.payment.PaymentGatewayService;
import ua.lviv.bas.cinema.service.shared.NumberGeneratorService;
import ua.lviv.bas.cinema.service.user.BonusService;

@ExtendWith(MockitoExtension.class)
public class PaymentProcessingServiceTest {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private BookingRepository bookingRepository;

	@Mock
	private PaymentValidator paymentValidator;

	@Mock
	private PaymentNotificationService notificationService;

	@Mock
	private PaymentGatewayService paymentGatewayService;

	@Mock
	private TicketService ticketService;

	@Mock
	private BonusService bonusService;

	@Mock
	private NumberGeneratorService numberGenerator;

	@Mock
	private BookingManagementService bookingManagementService;

	@InjectMocks
	private PaymentProcessingService paymentProcessingService;

	private User testUser;
	private Session testSession;
	private Booking testBooking;
	private Payment testPayment;
	private PaymentCreateRequest createRequest;

	private static final Long USER_ID = 1L;
	private static final Long BOOKING_ID = 2L;
	private static final Long PAYMENT_ID = 3L;
	private static final BigDecimal AMOUNT = new BigDecimal("200.00");

	@BeforeEach
	void setUp() {
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

		testBooking = new Booking();
		testBooking.setId(BOOKING_ID);
		testBooking.setUser(testUser);
		testBooking.setSession(testSession);
		testBooking.setStatus(BookingStatus.PENDING);
		testBooking.setFinalPrice(AMOUNT);
		testBooking.setExpiresAt(LocalDateTime.now().plusHours(1));

		testPayment = new Payment();
		testPayment.setId(PAYMENT_ID);
		testPayment.setBooking(testBooking);
		testPayment.setAmount(AMOUNT);
		testPayment.setStatus(PaymentStatus.PENDING);
		testPayment.setLiqpayOrderId("ORD_TEST123456789");

		createRequest = new PaymentCreateRequest();
		createRequest.setBookingId(BOOKING_ID);
	}

	@Test
	void createPayment_Success() {
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));
		when(paymentRepository.findByBookingId(BOOKING_ID)).thenReturn(Optional.empty());
		when(numberGenerator.generateLiqpayOrderId()).thenReturn("ORD_NEW123456789");
		when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
		when(numberGenerator.generateBookingNumber(testBooking)).thenReturn("BK-2024-00001");

		PaymentResponse response = paymentProcessingService.createPayment(createRequest, testUser);

		assertThat(response).isNotNull();
		verify(paymentValidator).validateBookingForPayment(testBooking);
	}

	@Test
	void createPayment_WhenBookingNotFound_ShouldThrowException() {
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> paymentProcessingService.createPayment(createRequest, testUser))
				.isInstanceOf(BookingNotFoundException.class);
	}

	@Test
	void createPayment_WhenPaymentInProgress_ShouldThrowException() {
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));
		when(paymentRepository.findByBookingId(BOOKING_ID)).thenReturn(Optional.of(testPayment));

		assertThatThrownBy(() -> paymentProcessingService.createPayment(createRequest, testUser))
				.isInstanceOf(PaymentProcessingException.class);
	}

	@Test
	void getPaymentStatus_Success() {
		when(paymentRepository.findByIdWithDetails(PAYMENT_ID)).thenReturn(Optional.of(testPayment));
		when(numberGenerator.generateBookingNumber(testBooking)).thenReturn("BK-2024-00001");

		PaymentResponse response = paymentProcessingService.getPaymentStatus(PAYMENT_ID, testUser);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(PAYMENT_ID);
	}

	@Test
	void getPaymentStatus_WhenPaymentNotFound_ShouldThrowException() {
		when(paymentRepository.findByIdWithDetails(PAYMENT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> paymentProcessingService.getPaymentStatus(PAYMENT_ID, testUser))
				.isInstanceOf(PaymentNotFoundException.class);
	}

	@Test
	void getPaymentStatus_WhenUserNotAuthorized_ShouldThrowException() {
		User otherUser = new User();
		otherUser.setId(999L);

		when(paymentRepository.findByIdWithDetails(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

		assertThatThrownBy(() -> paymentProcessingService.getPaymentStatus(PAYMENT_ID, otherUser))
				.isInstanceOf(PaymentAccessDeniedException.class);
	}

	@Test
	void processSuccessfulPayment_Success() {
		Map<String, String> callbackData = new HashMap<>();
		callbackData.put("payment_id", "PAY123");

		List<Ticket> tickets = Arrays.asList(new Ticket());

		when(ticketService.createTicketsForBooking(testBooking, testPayment)).thenReturn(tickets);
		when(bonusService.calculatePoints(AMOUNT)).thenReturn(20);

		paymentProcessingService.processSuccessfulPayment(testPayment, callbackData);

		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
		verify(bookingManagementService).confirmBooking(BOOKING_ID);
		verify(ticketService).createTicketsForBooking(testBooking, testPayment);
		verify(bonusService).accruePoints(USER_ID, 20, testBooking, testPayment);
	}

	@Test
	void processFailedPayment_Success() {
		Map<String, String> callbackData = new HashMap<>();
		callbackData.put("err_description", "Insufficient funds");

		paymentProcessingService.processFailedPayment(testPayment, callbackData);

		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
		verify(notificationService).sendPaymentFailedEmail(testPayment, testBooking);
	}

	@Test
	void retryPayment_Success() {
		testPayment.setStatus(PaymentStatus.FAILED);

		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(testPayment));
		when(numberGenerator.generateLiqpayOrderId()).thenReturn("ORD_NEW789");
		when(paymentRepository.save(testPayment)).thenReturn(testPayment);
		when(numberGenerator.generateBookingNumber(testBooking)).thenReturn("BK-2024-00001");

		PaymentResponse response = paymentProcessingService.retryPayment(PAYMENT_ID, testUser);

		assertThat(response).isNotNull();
		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
		verify(paymentValidator).validateBookingForPayment(testBooking);
	}

	@Test
	void retryPayment_WhenPaymentNotFound_ShouldThrowException() {
		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> paymentProcessingService.retryPayment(PAYMENT_ID, testUser))
				.isInstanceOf(PaymentNotFoundException.class);
	}

	@Test
	void retryPayment_WhenPaymentNotFailed_ShouldThrowException() {
		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

		assertThatThrownBy(() -> paymentProcessingService.retryPayment(PAYMENT_ID, testUser))
				.isInstanceOf(InvalidPaymentStatusException.class);
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

		paymentProcessingService.refundPayment(testPayment, refundAmount, description);

		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.PARTIALLY_REFUNDED);
		verify(paymentGatewayService).processRefund("refund_data");
	}

	@Test
	void refundPayment_WhenPaymentNotSuccess_ShouldThrowException() {
		testPayment.setStatus(PaymentStatus.PENDING);
		BigDecimal refundAmount = new BigDecimal("100.00");
		String description = "Test refund";

		assertThatThrownBy(() -> paymentProcessingService.refundPayment(testPayment, refundAmount, description))
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

		paymentProcessingService.refundPayment(testPayment, refundAmount, description);

		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
	}
}