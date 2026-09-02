package ua.lviv.bas.cinema.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.booking.domain.SeatReservation;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.booking.domain.status.ReservationStatus;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.payment.dto.request.PaymentCreateRequest;
import ua.lviv.bas.cinema.payment.dto.response.PaymentResponse;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.payment.InvalidPaymentStatusException;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentAccessDeniedException;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.payment.repository.PaymentRepository;
import ua.lviv.bas.cinema.common.DateTimeFormatterService;
import ua.lviv.bas.cinema.common.NumberGeneratorService;
import ua.lviv.bas.cinema.audit.service.AuditService;
import ua.lviv.bas.cinema.notification.service.EmailService;
import ua.lviv.bas.cinema.support.CinemaTestFixtures;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private NumberGeneratorService numberGenerator;
    @Mock
    private PaymentSuccessOrchestrator paymentSuccessOrchestrator;
    @Mock
    private DateTimeFormatterService dateTimeFormatter;
    @Mock
    private EmailService emailService;
    @Mock
    private AuditService auditService;

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

        var movie = CinemaTestFixtures.movie();
        var hall = CinemaTestFixtures.hall();
        var session = CinemaTestFixtures.session(movie, hall);

        Seat seat = Seat.builder().row(1).number(1).build();
        SeatReservation seatReservation = SeatReservation.builder().seat(seat)
                .status(ReservationStatus.CONFIRMED).build();

        testBooking = Booking.builder().id(BOOKING_ID).user(testUser).session(session).status(BookingStatus.PENDING)
                .finalPrice(AMOUNT).expiresAt(LocalDateTime.now().plusHours(1))
                .seatReservations(Collections.singletonList(seatReservation)).build();

        testPayment = Payment.builder().id(PAYMENT_ID).booking(testBooking).amount(AMOUNT).status(PaymentStatus.PENDING)
                .liqpayOrderId("ORD_TEST123456789").build();

        createRequest = new PaymentCreateRequest(BOOKING_ID);

        lenient().doAnswer(invocation -> {
            Runnable emailAction = invocation.getArgument(2);
            emailAction.run();
            return null;
        }).when(emailService).sendSafely(any(String.class), any(), any());
    }

    @Test
    void createPaymentShouldSucceed() {
        when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));
        when(paymentRepository.findByBookingId(BOOKING_ID)).thenReturn(Optional.empty());
        when(numberGenerator.generateLiqpayOrderId()).thenReturn("ORD_NEW123456789");
        when(numberGenerator.generateBookingNumber(testBooking)).thenReturn("BK-2024-00001");
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        PaymentResponse response = paymentService.createPayment(createRequest, testUser);

        assertThat(response).isNotNull();
        assertThat(response.bookingNumber()).isEqualTo("BK-2024-00001");
        assertThat(response.movieTitle()).isEqualTo("Test Movie");
        assertThat(response.hallName()).isEqualTo("Hall A");
        assertThat(response.finalAmount()).isEqualTo(AMOUNT);
        assertThat(response.status()).isEqualTo(PaymentStatus.PENDING);

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createPaymentWhenBookingNotFoundShouldThrowException() {
        when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createPayment(createRequest, testUser))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createPaymentWhenAlreadyExistsShouldReturnExisting() {
        when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));
        when(paymentRepository.findByBookingId(BOOKING_ID)).thenReturn(Optional.of(testPayment));
        when(numberGenerator.generateBookingNumber(testBooking)).thenReturn("BK-2024-00001");

        PaymentResponse response = paymentService.createPayment(createRequest, testUser);

        assertThat(response).isNotNull();
        assertThat(response.bookingNumber()).isEqualTo("BK-2024-00001");
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void getPaymentShouldSucceed() {
        when(paymentRepository.findByIdWithDetails(PAYMENT_ID)).thenReturn(Optional.of(testPayment));
        when(numberGenerator.generateBookingNumber(testBooking)).thenReturn("BK-2024-00001");

        PaymentResponse response = paymentService.getPayment(PAYMENT_ID, testUser);

        assertThat(response).isNotNull();
        assertThat(response.bookingNumber()).isEqualTo("BK-2024-00001");
        assertThat(response.movieTitle()).isEqualTo("Test Movie");
        assertThat(response.hallName()).isEqualTo("Hall A");
        assertThat(response.finalAmount()).isEqualTo(AMOUNT);
        assertThat(response.status()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void getPaymentWhenPaymentNotFoundShouldThrowException() {
        when(paymentRepository.findByIdWithDetails(PAYMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPayment(PAYMENT_ID, testUser))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getPaymentWhenUserNotAuthorizedShouldThrowException() {
        User otherUser = User.builder().id(999L).build();

        when(paymentRepository.findByIdWithDetails(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

        assertThatThrownBy(() -> paymentService.getPayment(PAYMENT_ID, otherUser))
                .isInstanceOf(PaymentAccessDeniedException.class);
    }

    @Test
    void retryPaymentShouldSucceed() {
        testPayment.setStatus(PaymentStatus.FAILED);

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(testPayment));
        when(numberGenerator.generateLiqpayOrderId()).thenReturn("ORD_NEW789");
        when(numberGenerator.generateBookingNumber(testBooking)).thenReturn("BK-2024-00001");
        when(paymentRepository.save(testPayment)).thenReturn(testPayment);

        PaymentResponse response = paymentService.retryPayment(PAYMENT_ID, testUser);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void retryPaymentWhenPaymentNotFoundShouldThrowException() {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.retryPayment(PAYMENT_ID, testUser))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void retryPaymentWhenPaymentNotFailedShouldThrowException() {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(testPayment));

        assertThatThrownBy(() -> paymentService.retryPayment(PAYMENT_ID, testUser))
                .isInstanceOf(InvalidPaymentStatusException.class);
    }

    @Test
    void processSuccessShouldSucceed() {
        Map<String, String> callbackData = new HashMap<>();
        callbackData.put("payment_id", "PAY123");
        callbackData.put("transaction_id", "TXN123");
        callbackData.put("sender_card_mask", "****1234");

        paymentService.processSuccess(testPayment, callbackData);

        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(testPayment.getLiqpayPaymentId()).isEqualTo("PAY123");
        assertThat(testPayment.getLiqpaySenderCardMask()).isEqualTo("****1234");
        assertThat(testPayment.getPaymentTime()).isNotNull();

        verify(paymentSuccessOrchestrator).handle(testPayment);
    }

    @Test
    void processSuccessWhenAlreadySuccessShouldIgnoreDuplicateCallback() {
        testPayment.setStatus(PaymentStatus.SUCCESS);
        testPayment.setLiqpayPaymentId("PAY_ORIGINAL");
        testPayment.setLiqpayTransactionId("TXN_ORIGINAL");

        Map<String, String> callbackData = new HashMap<>();
        callbackData.put("payment_id", "PAY_DUPLICATE");
        callbackData.put("transaction_id", "TXN_DUPLICATE");
        callbackData.put("sender_card_mask", "****9999");

        paymentService.processSuccess(testPayment, callbackData);

        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(testPayment.getLiqpayPaymentId()).isEqualTo("PAY_ORIGINAL");
        assertThat(testPayment.getLiqpayTransactionId()).isEqualTo("TXN_ORIGINAL");

        verify(paymentSuccessOrchestrator, never()).handle(any(Payment.class));
        verify(auditService, never()).logChange(anyString(), anyLong(), anyString(), any(), any(), any());
    }

    @Test
    void processFailureShouldSucceed() {
        Map<String, String> callbackData = new HashMap<>();
        callbackData.put("err_code", "ERR_001");
        callbackData.put("err_description", "Insufficient funds");

        when(dateTimeFormatter.formatStandard(any(LocalDateTime.class))).thenReturn("2024-01-01 14:00");
        when(numberGenerator.generateBookingNumber(testBooking)).thenReturn("BK-2024-00001");

        paymentService.processFailure(testPayment, callbackData);

        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(testPayment.getLiqpayErrorCode()).isEqualTo("ERR_001");
        assertThat(testPayment.getLiqpayErrorDescription()).isEqualTo("Insufficient funds");
    }

    @Test
    void processFailureWhenAlreadyFailedShouldIgnoreDuplicateCallback() {
        testPayment.setStatus(PaymentStatus.FAILED);
        testPayment.setLiqpayErrorCode("ERR_ORIGINAL");
        testPayment.setLiqpayErrorDescription("Original error");

        Map<String, String> callbackData = new HashMap<>();
        callbackData.put("err_code", "ERR_DUPLICATE");
        callbackData.put("err_description", "Duplicate error");

        paymentService.processFailure(testPayment, callbackData);

        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(testPayment.getLiqpayErrorCode()).isEqualTo("ERR_ORIGINAL");
        assertThat(testPayment.getLiqpayErrorDescription()).isEqualTo("Original error");

        verify(emailService, never()).sendSafely(any(String.class), any(), any());
        verify(auditService, never()).logChange(anyString(), anyLong(), anyString(), any(), any(), any());
    }
}