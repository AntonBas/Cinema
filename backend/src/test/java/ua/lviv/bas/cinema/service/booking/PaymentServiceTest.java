package ua.lviv.bas.cinema.service.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
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
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.BookedSeatStatus;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;
import ua.lviv.bas.cinema.dto.payment.request.PaymentCreateRequest;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.exception.domain.booking.SessionTooCloseException;
import ua.lviv.bas.cinema.exception.domain.payment.InvalidPaymentStatusException;
import ua.lviv.bas.cinema.exception.domain.payment.PaymentAccessDeniedException;
import ua.lviv.bas.cinema.exception.domain.payment.PaymentNotFoundException;
import ua.lviv.bas.cinema.exception.domain.payment.PaymentProcessingException;
import ua.lviv.bas.cinema.repository.BookingRepository;
import ua.lviv.bas.cinema.repository.PaymentRepository;
import ua.lviv.bas.cinema.service.notification.EmailService;
import ua.lviv.bas.cinema.service.user.BonusService;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private BookingRepository bookingRepository;

	@Mock
	private EmailService emailService;

	@Mock
	private TicketService ticketService;

	@Mock
	private BonusService bonusService;

	@Mock
	private BookingService bookingService;

	@InjectMocks
	private PaymentService paymentService;

	private User testUser;
	private Booking testBooking;
	private Payment testPayment;
	private PaymentCreateRequest createRequest;
	private Session testSession;
	private BookedSeat bookedSeat;

	private final Long USER_ID = 1L;
	private final Long BOOKING_ID = 2L;
	private final Long PAYMENT_ID = 3L;
	private final Long SESSION_ID = 4L;
	private final BigDecimal AMOUNT = new BigDecimal("380.00");
	private final LocalDateTime CREATED_AT = LocalDateTime.of(2024, 1, 1, 10, 0);

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(paymentService, "liqpayPublicKey", "test_public_key");
		ReflectionTestUtils.setField(paymentService, "liqpayPrivateKey", "test_private_key");
		ReflectionTestUtils.setField(paymentService, "frontendUrl", "http://localhost:3000");
		ReflectionTestUtils.setField(paymentService, "liqpayCallbackUrl",
				"http://localhost:8080/api/payments/callback");
		ReflectionTestUtils.setField(paymentService, "sandboxMode", true);

		testUser = new User();
		testUser.setId(USER_ID);
		testUser.setEmail("test@example.com");
		testUser.setFirstName("John");
		testUser.setLastName("Doe");

		Movie movie = new Movie();
		movie.setTitle("Test Movie");

		CinemaHall hall = new CinemaHall();
		hall.setName("Hall A");

		testSession = new Session();
		testSession.setId(SESSION_ID);
		testSession.setMovie(movie);
		testSession.setHall(hall);
		testSession.setStartTime(LocalDateTime.now().plusHours(2));

		Seat seat = new Seat();
		seat.setRow(1);
		seat.setNumber(1);

		bookedSeat = new BookedSeat();
		bookedSeat.setId(1L);
		bookedSeat.setStatus(BookedSeatStatus.PENDING);
		bookedSeat.setSeat(seat);

		testBooking = Booking.builder().id(BOOKING_ID).user(testUser).session(testSession).status(BookingStatus.PENDING)
				.totalPrice(new BigDecimal("480.00")).finalPrice(AMOUNT).bonusPointsUsed(100)
				.expiresAt(LocalDateTime.now().plusHours(1)).createdAt(CREATED_AT)
				.bookedSeats(Arrays.asList(bookedSeat)).build();

		testPayment = Payment.builder().id(PAYMENT_ID).booking(testBooking).amount(AMOUNT).status(PaymentStatus.PENDING)
				.liqpayOrderId("ORD_TEST123456789").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
				.build();

		createRequest = new PaymentCreateRequest();
		createRequest.setBookingId(BOOKING_ID);
	}

	@Test
	void createPayment_Success() {
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));
		when(paymentRepository.findByBookingId(BOOKING_ID)).thenReturn(Optional.empty());
		when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

		PaymentResponse response = paymentService.createPayment(createRequest, testUser);

		assertThat(response).isNotNull();
		assertThat(response.getBookingId()).isEqualTo(BOOKING_ID);
		assertThat(response.getAmount()).isEqualByComparingTo(AMOUNT);
		assertThat(response.getStatus()).isEqualTo(PaymentStatus.PENDING);
		verify(bookingRepository).findByIdAndUserId(BOOKING_ID, USER_ID);
		verify(paymentRepository).save(any(Payment.class));
	}

	@Test
	void createPayment_WhenBookingNotFound_ShouldThrowException() {
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> paymentService.createPayment(createRequest, testUser))
				.isInstanceOf(BookingNotFoundException.class);

		verify(bookingRepository).findByIdAndUserId(BOOKING_ID, USER_ID);
		verify(paymentRepository, never()).save(any());
	}

	@Test
	void createPayment_WhenBookingNotPending_ShouldThrowException() {
		testBooking.setStatus(BookingStatus.CONFIRMED);
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));

		assertThatThrownBy(() -> paymentService.createPayment(createRequest, testUser))
				.isInstanceOf(PaymentProcessingException.class);

		verify(bookingRepository).findByIdAndUserId(BOOKING_ID, USER_ID);
		verify(paymentRepository, never()).save(any());
	}

	@Test
	void createPayment_WhenBookingExpired_ShouldThrowException() {
		testBooking.setExpiresAt(LocalDateTime.now().minusMinutes(10));
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));

		assertThatThrownBy(() -> paymentService.createPayment(createRequest, testUser))
				.isInstanceOf(PaymentProcessingException.class);

		verify(bookingRepository).findByIdAndUserId(BOOKING_ID, USER_ID);
		verify(paymentRepository, never()).save(any());
	}

	@Test
	void createPayment_WhenSessionTooClose_ShouldThrowException() {
		testSession.setStartTime(LocalDateTime.now().plusMinutes(20));
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));

		assertThatThrownBy(() -> paymentService.createPayment(createRequest, testUser))
				.isInstanceOf(SessionTooCloseException.class);

		verify(bookingRepository).findByIdAndUserId(BOOKING_ID, USER_ID);
		verify(paymentRepository, never()).save(any());
	}

	@Test
	void createPayment_WhenSeatsNotAvailable_ShouldThrowException() {
		bookedSeat.setStatus(BookedSeatStatus.CONFIRMED);
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));

		assertThatThrownBy(() -> paymentService.createPayment(createRequest, testUser))
				.isInstanceOf(PaymentProcessingException.class);

		verify(bookingRepository).findByIdAndUserId(BOOKING_ID, USER_ID);
		verify(paymentRepository, never()).save(any());
	}

	@Test
	void createPayment_WhenPaymentAlreadyPending_ShouldThrowException() {
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));
		when(paymentRepository.findByBookingId(BOOKING_ID)).thenReturn(Optional.of(testPayment));

		assertThatThrownBy(() -> paymentService.createPayment(createRequest, testUser))
				.isInstanceOf(PaymentProcessingException.class);

		verify(bookingRepository).findByIdAndUserId(BOOKING_ID, USER_ID);
		verify(paymentRepository, never()).save(any());
	}

	@Test
	void preparePaymentData_Success() {
		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

		PaymentLiqPayDataResponse response = paymentService.preparePaymentData(PAYMENT_ID, testUser);

		assertThat(response).isNotNull();
		assertThat(response.getLiqpayOrderId()).isEqualTo("ORD_TEST123456789");
		assertThat(response.getPaymentUrl()).isEqualTo("https://www.liqpay.ua/api/3/checkout");
		assertThat(response.getData()).isNotBlank();
		assertThat(response.getSignature()).isNotBlank();
		verify(paymentRepository).findById(PAYMENT_ID);
	}

	@Test
	void preparePaymentData_WhenPaymentNotFound_ShouldThrowException() {
		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> paymentService.preparePaymentData(PAYMENT_ID, testUser))
				.isInstanceOf(PaymentNotFoundException.class);
	}

	@Test
	void preparePaymentData_WhenUserNotAuthorized_ShouldThrowException() {
		User otherUser = new User();
		otherUser.setId(999L);

		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

		assertThatThrownBy(() -> paymentService.preparePaymentData(PAYMENT_ID, otherUser))
				.isInstanceOf(PaymentAccessDeniedException.class);
	}

	@Test
	void preparePaymentData_WhenPaymentNotPending_ShouldThrowException() {
		testPayment.setStatus(PaymentStatus.SUCCESS);
		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

		assertThatThrownBy(() -> paymentService.preparePaymentData(PAYMENT_ID, testUser))
				.isInstanceOf(InvalidPaymentStatusException.class);
	}

	@Test
	void getPaymentStatus_Success() {
		when(paymentRepository.findByIdWithDetails(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

		PaymentResponse response = paymentService.getPaymentStatus(PAYMENT_ID, testUser);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(PAYMENT_ID);
		assertThat(response.getBookingId()).isEqualTo(BOOKING_ID);
		verify(paymentRepository).findByIdWithDetails(PAYMENT_ID);
	}

	@Test
	void getPaymentStatus_WhenPaymentNotFound_ShouldThrowException() {
		when(paymentRepository.findByIdWithDetails(PAYMENT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> paymentService.getPaymentStatus(PAYMENT_ID, testUser))
				.isInstanceOf(PaymentNotFoundException.class);
	}

	@Test
	void getPaymentStatus_WhenUserNotAuthorized_ShouldThrowException() {
		User otherUser = new User();
		otherUser.setId(999L);

		when(paymentRepository.findByIdWithDetails(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

		assertThatThrownBy(() -> paymentService.getPaymentStatus(PAYMENT_ID, otherUser))
				.isInstanceOf(PaymentAccessDeniedException.class);
	}

	@Test
	void retryPayment_Success() {
		testPayment.setStatus(PaymentStatus.FAILED);

		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(testPayment));
		when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
		when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

		PaymentResponse response = paymentService.retryPayment(PAYMENT_ID, testUser);

		assertThat(response).isNotNull();
		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.PENDING);
		assertThat(testPayment.getLiqpayOrderId()).isNotBlank();
		assertThat(testBooking.getStatus()).isEqualTo(BookingStatus.PENDING);
		verify(paymentRepository).save(testPayment);
		verify(bookingRepository).save(testBooking);
	}

	@Test
	void retryPayment_WhenPaymentNotFound_ShouldThrowException() {
		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> paymentService.retryPayment(PAYMENT_ID, testUser))
				.isInstanceOf(PaymentNotFoundException.class);

		verify(paymentRepository).findById(PAYMENT_ID);
		verify(paymentRepository, never()).save(any());
	}

	@Test
	void retryPayment_WhenUserNotAuthorized_ShouldThrowException() {
		User otherUser = new User();
		otherUser.setId(999L);

		testPayment.setStatus(PaymentStatus.FAILED);
		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

		assertThatThrownBy(() -> paymentService.retryPayment(PAYMENT_ID, otherUser))
				.isInstanceOf(PaymentAccessDeniedException.class);
	}

	@Test
	void retryPayment_WhenPaymentNotFailed_ShouldThrowException() {
		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

		assertThatThrownBy(() -> paymentService.retryPayment(PAYMENT_ID, testUser))
				.isInstanceOf(InvalidPaymentStatusException.class);
	}

	@Test
	void refundPayment_Success() {
		testPayment.setStatus(PaymentStatus.SUCCESS);
		BigDecimal refundAmount = new BigDecimal("100.00");
		String description = "Test refund";

		when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

		paymentService.refundPayment(testPayment, refundAmount, description);

		verify(paymentRepository).save(testPayment);
		verify(emailService).sendRefundEmail(eq("test@example.com"), eq("2"), eq("Test Movie"), anyString(),
				eq("Hall A"), eq(refundAmount), eq("Row 1, Seat 1"), eq(description));
	}

	@Test
	void refundPayment_WhenPaymentNotSuccess_ShouldThrowException() {
		testPayment.setStatus(PaymentStatus.PENDING);
		BigDecimal refundAmount = new BigDecimal("100.00");
		String description = "Test refund";

		assertThatThrownBy(() -> paymentService.refundPayment(testPayment, refundAmount, description))
				.isInstanceOf(PaymentProcessingException.class);

		verify(paymentRepository, never()).save(any());
		verify(emailService, never()).sendRefundEmail(any(), any(), any(), any(), any(), any(), any(), any());
	}

	@Test
	void refundPayment_WhenAmountZero_ShouldThrowException() {
		testPayment.setStatus(PaymentStatus.SUCCESS);
		BigDecimal refundAmount = BigDecimal.ZERO;
		String description = "Test refund";

		assertThatThrownBy(() -> paymentService.refundPayment(testPayment, refundAmount, description))
				.isInstanceOf(PaymentProcessingException.class);

		verify(paymentRepository, never()).save(any());
		verify(emailService, never()).sendRefundEmail(any(), any(), any(), any(), any(), any(), any(), any());
	}

	@Test
	void refundPayment_WhenAmountExceedsPayment_ShouldThrowException() {
		testPayment.setStatus(PaymentStatus.SUCCESS);
		BigDecimal refundAmount = AMOUNT.add(new BigDecimal("100.00"));
		String description = "Test refund";

		assertThatThrownBy(() -> paymentService.refundPayment(testPayment, refundAmount, description))
				.isInstanceOf(PaymentProcessingException.class);

		verify(paymentRepository, never()).save(any());
		verify(emailService, never()).sendRefundEmail(any(), any(), any(), any(), any(), any(), any(), any());
	}

	@Test
	void getUserPaymentByBookingId_Success() {
		when(paymentRepository.findByBookingIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testPayment));

		PaymentResponse response = paymentService.getUserPaymentByBookingId(BOOKING_ID, testUser);

		assertThat(response).isNotNull();
		assertThat(response.getBookingId()).isEqualTo(BOOKING_ID);
		assertThat(response.getId()).isEqualTo(PAYMENT_ID);
		verify(paymentRepository).findByBookingIdAndUserId(BOOKING_ID, USER_ID);
	}

	@Test
	void getUserPaymentByBookingId_WhenPaymentNotFound_ShouldThrowException() {
		when(paymentRepository.findByBookingIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> paymentService.getUserPaymentByBookingId(BOOKING_ID, testUser))
				.isInstanceOf(PaymentNotFoundException.class);
	}
}