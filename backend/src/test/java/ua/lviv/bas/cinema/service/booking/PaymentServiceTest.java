package ua.lviv.bas.cinema.service.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.BookedSeatStatus;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;
import ua.lviv.bas.cinema.dto.payment.request.LiqPayCallbackRequest;
import ua.lviv.bas.cinema.dto.payment.request.PaymentCreateRequest;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.exception.domain.payment.PaymentProcessingException;
import ua.lviv.bas.cinema.repository.BookingRepository;
import ua.lviv.bas.cinema.repository.PaymentRepository;
import ua.lviv.bas.cinema.service.notification.EmailService;
import ua.lviv.bas.cinema.service.user.BonusService;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

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

	@InjectMocks
	private PaymentService paymentService;

	private User testUser;
	private Booking testBooking;
	private Payment testPayment;
	private PaymentCreateRequest createRequest;
	private Session testSession;
	private BookedSeat bookedSeat;
	private LiqPayCallbackRequest callbackRequest;

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
		ReflectionTestUtils.setField(paymentService, "liqpayServerUrl", "http://localhost:8080/api/payments/callback");

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

		bookedSeat = new BookedSeat();
		bookedSeat.setId(1L);
		bookedSeat.setStatus(BookedSeatStatus.PENDING);

		testBooking = Booking.builder().id(BOOKING_ID).user(testUser).session(testSession).status(BookingStatus.PENDING)
				.totalPrice(new BigDecimal("480.00")).finalPrice(AMOUNT).bonusPointsUsed(100)
				.expiresAt(LocalDateTime.now().plusHours(1)).createdAt(CREATED_AT)
				.bookedSeats(Arrays.asList(bookedSeat)).build();

		testPayment = Payment.builder().id(PAYMENT_ID).booking(testBooking).amount(AMOUNT).status(PaymentStatus.PENDING)
				.liqpayOrderId("ORD_TEST123456789").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
				.build();

		createRequest = new PaymentCreateRequest();
		createRequest.setBookingId(BOOKING_ID);

		callbackRequest = new LiqPayCallbackRequest();
		callbackRequest.setOrderId("ORD_TEST123456789");
		callbackRequest.setStatus("success");
		callbackRequest.setPaymentId("123456");
		callbackRequest.setTransactionId("TRX123456");
		callbackRequest.setSenderCardMask("************1234");
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
				.isInstanceOf(PaymentProcessingException.class).hasMessage("Booking is not in pending status");

		verify(bookingRepository).findByIdAndUserId(BOOKING_ID, USER_ID);
		verify(paymentRepository, never()).save(any());
	}

	@Test
	void createPayment_WhenBookingExpired_ShouldThrowException() {
		testBooking.setExpiresAt(LocalDateTime.now().minusMinutes(10));
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));

		assertThatThrownBy(() -> paymentService.createPayment(createRequest, testUser))
				.isInstanceOf(PaymentProcessingException.class).hasMessageContaining("expired");

		verify(bookingRepository).findByIdAndUserId(BOOKING_ID, USER_ID);
		verify(paymentRepository, never()).save(any());
	}

	@Test
	void createPayment_WhenSessionTooClose_ShouldThrowException() {
		testSession.setStartTime(LocalDateTime.now().plusMinutes(20));
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));

		assertThatThrownBy(() -> paymentService.createPayment(createRequest, testUser))
				.isInstanceOf(PaymentProcessingException.class).hasMessageContaining("less than 30 minutes");

		verify(bookingRepository).findByIdAndUserId(BOOKING_ID, USER_ID);
		verify(paymentRepository, never()).save(any());
	}

	@Test
	void createPayment_WhenSeatsNotAvailable_ShouldThrowException() {
		bookedSeat.setStatus(BookedSeatStatus.CONFIRMED);
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));

		assertThatThrownBy(() -> paymentService.createPayment(createRequest, testUser))
				.isInstanceOf(PaymentProcessingException.class).hasMessageContaining("no longer available");

		verify(bookingRepository).findByIdAndUserId(BOOKING_ID, USER_ID);
		verify(paymentRepository, never()).save(any());
	}

	@Test
	void createPayment_WhenPaymentAlreadyPending_ShouldThrowException() {
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));
		when(paymentRepository.findByBookingId(BOOKING_ID)).thenReturn(Optional.of(testPayment));

		assertThatThrownBy(() -> paymentService.createPayment(createRequest, testUser))
				.isInstanceOf(PaymentProcessingException.class).hasMessageContaining("already in progress");

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
				.isInstanceOf(PaymentProcessingException.class).hasMessageContaining("Payment not found");

		verify(paymentRepository).findById(PAYMENT_ID);
	}

	@Test
	void preparePaymentData_WhenAccessDenied_ShouldThrowException() {
		User otherUser = new User();
		otherUser.setId(999L);

		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

		assertThatThrownBy(() -> paymentService.preparePaymentData(PAYMENT_ID, otherUser))
				.isInstanceOf(PaymentProcessingException.class).hasMessageContaining("Access denied");

		verify(paymentRepository).findById(PAYMENT_ID);
	}

	@Test
	void preparePaymentData_WhenPaymentNotPending_ShouldThrowException() {
		testPayment.setStatus(PaymentStatus.SUCCESS);
		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

		assertThatThrownBy(() -> paymentService.preparePaymentData(PAYMENT_ID, testUser))
				.isInstanceOf(PaymentProcessingException.class).hasMessageContaining("not pending");

		verify(paymentRepository).findById(PAYMENT_ID);
	}

	@Test
	void processLiqPayCallback_Success() {
		when(paymentRepository.findByLiqpayOrderId("ORD_TEST123456789")).thenReturn(Optional.of(testPayment));
		when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
		when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
		when(ticketService.createTicketsForBooking(any(Booking.class), any(Payment.class)))
				.thenReturn(Collections.singletonList(new Ticket()));

		paymentService.processLiqPayCallback(callbackRequest);

		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
		assertThat(testPayment.getLiqpayPaymentId()).isEqualTo("123456");
		assertThat(testBooking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
		assertThat(bookedSeat.getStatus()).isEqualTo(BookedSeatStatus.CONFIRMED);

		verify(paymentRepository).save(testPayment);
		verify(bookingRepository).save(testBooking);
		verify(ticketService).createTicketsForBooking(testBooking, testPayment);
		verify(ticketService).sendTicketsToUser(testBooking);
		verify(bonusService).redeemPointsForPurchase(eq(USER_ID), eq(100), any(Payment.class), eq(AMOUNT));
	}

	@Test
	void processLiqPayCallback_Failure() {
		callbackRequest.setStatus("failure");
		callbackRequest.setErrorCode("100");
		callbackRequest.setErrorDescription("Insufficient funds");

		when(paymentRepository.findByLiqpayOrderId("ORD_TEST123456789")).thenReturn(Optional.of(testPayment));
		when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
		when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

		paymentService.processLiqPayCallback(callbackRequest);

		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
		assertThat(testPayment.getLiqpayErrorCode()).isEqualTo("100");
		assertThat(testPayment.getLiqpayErrorDescription()).isEqualTo("Insufficient funds");
		verify(emailService).sendPaymentFailedEmail(anyString(), anyString(), anyString(), anyString(), anyString());
	}

	@Test
	void processLiqPayCallback_WaitSecure() {
		callbackRequest.setStatus("wait_secure");

		when(paymentRepository.findByLiqpayOrderId("ORD_TEST123456789")).thenReturn(Optional.of(testPayment));
		when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

		paymentService.processLiqPayCallback(callbackRequest);

		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
		verify(paymentRepository).save(testPayment);
	}

	@Test
	void processLiqPayCallback_Sandbox() {
		callbackRequest.setStatus("sandbox");

		when(paymentRepository.findByLiqpayOrderId("ORD_TEST123456789")).thenReturn(Optional.of(testPayment));
		when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
		when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
		when(ticketService.createTicketsForBooking(any(Booking.class), any(Payment.class)))
				.thenReturn(Collections.singletonList(new Ticket()));

		paymentService.processLiqPayCallback(callbackRequest);

		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
		verify(paymentRepository).save(testPayment);
	}

	@Test
	void processLiqPayCallback_UnknownStatus() {
		callbackRequest.setStatus("unknown");

		when(paymentRepository.findByLiqpayOrderId("ORD_TEST123456789")).thenReturn(Optional.of(testPayment));
		when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

		paymentService.processLiqPayCallback(callbackRequest);

		assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
		verify(paymentRepository).save(testPayment);
	}

	@Test
	void processLiqPayCallback_WhenPaymentNotFound_ShouldThrowException() {
		when(paymentRepository.findByLiqpayOrderId("ORD_TEST123456789")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> paymentService.processLiqPayCallback(callbackRequest))
				.isInstanceOf(PaymentProcessingException.class).hasMessageContaining("Payment not found");

		verify(paymentRepository, never()).save(any());
	}

	@Test
	void getPaymentStatus_Success() {
		when(paymentRepository.findByIdWithDetails(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

		PaymentResponse response = paymentService.getPaymentStatus(PAYMENT_ID, testUser);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(PAYMENT_ID);
		assertThat(response.getBookingId()).isEqualTo(BOOKING_ID);
		assertThat(response.getBookingNumber()).isEqualTo("BK-2024-00002");
		verify(paymentRepository).findByIdWithDetails(PAYMENT_ID);
	}

	@Test
	void getPaymentStatus_WhenPaymentNotFound_ShouldThrowException() {
		when(paymentRepository.findByIdWithDetails(PAYMENT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> paymentService.getPaymentStatus(PAYMENT_ID, testUser))
				.isInstanceOf(PaymentProcessingException.class).hasMessageContaining("Payment not found");

		verify(paymentRepository).findByIdWithDetails(PAYMENT_ID);
	}

	@Test
	void getPaymentStatus_WhenAccessDenied_ShouldThrowException() {
		User otherUser = new User();
		otherUser.setId(999L);

		when(paymentRepository.findByIdWithDetails(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

		assertThatThrownBy(() -> paymentService.getPaymentStatus(PAYMENT_ID, otherUser))
				.isInstanceOf(PaymentProcessingException.class).hasMessageContaining("Access denied");

		verify(paymentRepository).findByIdWithDetails(PAYMENT_ID);
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
				.isInstanceOf(PaymentProcessingException.class).hasMessageContaining("Payment not found");

		verify(paymentRepository).findById(PAYMENT_ID);
		verify(paymentRepository, never()).save(any());
	}

	@Test
	void retryPayment_WhenAccessDenied_ShouldThrowException() {
		User otherUser = new User();
		otherUser.setId(999L);

		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

		assertThatThrownBy(() -> paymentService.retryPayment(PAYMENT_ID, otherUser))
				.isInstanceOf(PaymentProcessingException.class).hasMessageContaining("Access denied");

		verify(paymentRepository).findById(PAYMENT_ID);
		verify(paymentRepository, never()).save(any());
	}

	@Test
	void retryPayment_WhenNotFailed_ShouldThrowException() {
		testPayment.setStatus(PaymentStatus.SUCCESS);
		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

		assertThatThrownBy(() -> paymentService.retryPayment(PAYMENT_ID, testUser))
				.isInstanceOf(PaymentProcessingException.class).hasMessageContaining("Only failed payments");

		verify(paymentRepository).findById(PAYMENT_ID);
		verify(paymentRepository, never()).save(any());
	}

	@Test
	void getUserPayments_Success() {
		List<Payment> payments = Arrays.asList(testPayment);
		when(paymentRepository.findByUserId(USER_ID)).thenReturn(payments);

		List<PaymentResponse> responses = paymentService.getUserPayments(USER_ID);

		assertThat(responses).hasSize(1);
		assertThat(responses.get(0).getId()).isEqualTo(PAYMENT_ID);
		verify(paymentRepository).findByUserId(USER_ID);
	}

	@Test
	void validateBookingForPayment_Success() {
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));
		when(paymentRepository.findByBookingId(BOOKING_ID)).thenReturn(Optional.empty());
		when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

		paymentService.createPayment(createRequest, testUser);

		verify(bookingRepository).findByIdAndUserId(BOOKING_ID, USER_ID);
		verify(paymentRepository).save(any(Payment.class));
	}

	@Test
	void prepareLiqPayPaymentData_Success() {
		testBooking.setCreatedAt(CREATED_AT);
		when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

		PaymentLiqPayDataResponse response = paymentService.preparePaymentData(PAYMENT_ID, testUser);

		assertThat(response).isNotNull();
		assertThat(response.getData()).isNotBlank();
		assertThat(response.getSignature()).isNotBlank();
		assertThat(response.getPaymentUrl()).isEqualTo("https://www.liqpay.ua/api/3/checkout");
	}

	@Test
	void handleSuccessfulPayment_Success() {
		when(paymentRepository.findByLiqpayOrderId("ORD_TEST123456789")).thenReturn(Optional.of(testPayment));
		when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
		when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
		when(ticketService.createTicketsForBooking(any(Booking.class), any(Payment.class)))
				.thenReturn(Collections.singletonList(new Ticket()));

		paymentService.processLiqPayCallback(callbackRequest);

		verify(bonusService, times(1)).redeemPointsForPurchase(eq(USER_ID), eq(100), any(Payment.class), eq(AMOUNT));
		verify(ticketService, times(1)).sendTicketsToUser(testBooking);
	}

	@Test
	void handleFailedPayment_Success() {
		callbackRequest.setStatus("failure");
		callbackRequest.setErrorCode("100");
		callbackRequest.setErrorDescription("Insufficient funds");

		when(paymentRepository.findByLiqpayOrderId("ORD_TEST123456789")).thenReturn(Optional.of(testPayment));
		when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
		when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

		paymentService.processLiqPayCallback(callbackRequest);

		assertThat(testPayment.getLiqpayErrorCode()).isEqualTo("100");
		assertThat(testPayment.getLiqpayErrorDescription()).isEqualTo("Insufficient funds");
	}

	@Test
	void generateLiqpayOrderId_ShouldGenerateUniqueId() {
		String orderId1 = invokePrivateGenerateLiqpayOrderId();
		String orderId2 = invokePrivateGenerateLiqpayOrderId();

		assertThat(orderId1).isNotEqualTo(orderId2);
		assertThat(orderId1).startsWith("ORD_");
		assertThat(orderId1).hasSizeGreaterThan(10);
	}

	@Test
	void generateBookingNumber_Success() {
		testBooking.setId(BOOKING_ID);
		testBooking.setCreatedAt(CREATED_AT);

		String result = invokePrivateGenerateBookingNumber(testBooking);
		assertThat(result).isEqualTo("BK-2024-00002");
	}

	private String invokePrivateGenerateLiqpayOrderId() {
		try {
			var method = PaymentService.class.getDeclaredMethod("generateLiqpayOrderId");
			method.setAccessible(true);
			return (String) method.invoke(paymentService);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String invokePrivateGenerateBookingNumber(Booking booking) {
		try {
			var method = PaymentService.class.getDeclaredMethod("generateBookingNumber", Booking.class);
			method.setAccessible(true);
			return (String) method.invoke(paymentService, booking);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}