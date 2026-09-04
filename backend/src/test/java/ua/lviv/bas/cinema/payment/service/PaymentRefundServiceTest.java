package ua.lviv.bas.cinema.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.booking.domain.SeatReservation;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.ticket.domain.Ticket;
import ua.lviv.bas.cinema.ticket.domain.TicketType;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentProcessingException;
import ua.lviv.bas.cinema.payment.repository.PaymentRepository;
import ua.lviv.bas.cinema.common.DateTimeFormatterService;
import ua.lviv.bas.cinema.common.NumberGeneratorService;
import ua.lviv.bas.cinema.audit.service.AuditService;
import ua.lviv.bas.cinema.payment.service.PaymentGatewayService;
import ua.lviv.bas.cinema.notification.EmailService;
import ua.lviv.bas.cinema.support.CinemaTestFixtures;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentRefundServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentGatewayService paymentGatewayService;
    @Mock
    private AuditService auditService;
    @Mock
    private EmailService emailService;
    @Mock
    private DateTimeFormatterService dateTimeFormatter;
    @Mock
    private NumberGeneratorService numberGenerator;

    @InjectMocks
    private PaymentRefundService paymentRefundService;

    private User testUser;
    private Booking testBooking;
    private Payment testPayment;
    private Ticket testTicket;

    private static final BigDecimal AMOUNT = new BigDecimal("200.00");

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("test@example.com").build();

        var movie = CinemaTestFixtures.movie();
        var hall = CinemaTestFixtures.hall();
        var session = CinemaTestFixtures.session(movie, hall);

        Seat seat = Seat.builder().row(1).number(1).build();
        SeatReservation seatReservation = SeatReservation.builder().seat(seat).build();

        testBooking = Booking.builder().id(2L).user(testUser).session(session).finalPrice(AMOUNT).build();

        testPayment = Payment.builder().id(3L).booking(testBooking).amount(AMOUNT).status(PaymentStatus.PENDING)
                .liqpayOrderId("ORD_TEST123456789").build();

        TicketType ticketType = TicketType.builder().displayName("Standard").build();
        testTicket = Ticket.builder().id(1L).user(testUser).booking(testBooking).ticketType(ticketType)
                .seatReservation(seatReservation).build();

        lenient().doAnswer(invocation -> {
            Runnable emailAction = invocation.getArgument(2);
            emailAction.run();
            return null;
        }).when(emailService).sendSafely(any(String.class), any(), any());
    }

    @Test
    void validateRefundEligibilityWhenPaymentNotSuccessShouldThrowException() {
        testPayment.setStatus(PaymentStatus.PENDING);
        BigDecimal refundAmount = new BigDecimal("100.00");

        assertThatThrownBy(() -> paymentRefundService.validateRefundEligibility(testPayment, refundAmount))
                .isInstanceOf(PaymentProcessingException.class);
    }

    @Test
    void validateRefundEligibilityWhenEligibleShouldNotThrow() {
        testPayment.setStatus(PaymentStatus.SUCCESS);
        testPayment.setLiqpayPaymentId("PAY123");
        testPayment.setLiqpayOrderId("ORD_123");

        assertThatCode(() -> paymentRefundService.validateRefundEligibility(testPayment, testPayment.getAmount()))
                .doesNotThrowAnyException();
    }

    @Test
    void callLiqPayRefundShouldInvokeGateway() {
        BigDecimal refundAmount = new BigDecimal("100.00");
        String description = "Test refund";

        when(paymentGatewayService.prepareRefundData("PAY123", "ORD_123", refundAmount, description))
                .thenReturn("refund_data");

        paymentRefundService.callLiqPayRefund("PAY123", "ORD_123", refundAmount, description);

        verify(paymentGatewayService).processRefund("refund_data");
    }

    @Test
    void applyRefundSuccessShouldMarkPartiallyRefunded() {
        testPayment.setStatus(PaymentStatus.SUCCESS);
        BigDecimal refundAmount = new BigDecimal("100.00");
        String description = "Test refund";

        when(paymentRepository.save(testPayment)).thenReturn(testPayment);
        when(dateTimeFormatter.formatStandard(any(LocalDateTime.class))).thenReturn("2024-01-01 14:00");
        when(numberGenerator.generateBookingNumber(testBooking)).thenReturn("BK-2024-00001");

        paymentRefundService.applyRefundSuccess(testPayment, refundAmount, refundAmount, description, testTicket);

        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.PARTIALLY_REFUNDED);
        verify(paymentRepository).save(testPayment);
    }

    @Test
    void applyRefundSuccessWhenFullRefundShouldMarkAsFullyRefunded() {
        testPayment.setStatus(PaymentStatus.SUCCESS);
        BigDecimal refundAmount = AMOUNT;
        String description = "Full refund";

        when(paymentRepository.save(testPayment)).thenReturn(testPayment);
        when(dateTimeFormatter.formatStandard(any(LocalDateTime.class))).thenReturn("2024-01-01 14:00");
        when(numberGenerator.generateBookingNumber(testBooking)).thenReturn("BK-2024-00001");

        paymentRefundService.applyRefundSuccess(testPayment, refundAmount, refundAmount, description, testTicket);

        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void applyRefundSuccessWhenCumulativeRefundsCoverFullAmountShouldMarkAsFullyRefunded() {
        testPayment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
        BigDecimal refundAmount = AMOUNT.subtract(new BigDecimal("50.00"));
        String description = "Second refund for multi-ticket booking";

        when(paymentRepository.save(testPayment)).thenReturn(testPayment);
        when(dateTimeFormatter.formatStandard(any(LocalDateTime.class))).thenReturn("2024-01-01 14:00");
        when(numberGenerator.generateBookingNumber(testBooking)).thenReturn("BK-2024-00001");

        paymentRefundService.applyRefundSuccess(testPayment, refundAmount, AMOUNT, description, testTicket);

        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void applyRefundSuccessWhenAlreadyAppliedShouldSkip() {
        testPayment.setStatus(PaymentStatus.REFUNDED);
        BigDecimal refundAmount = AMOUNT;

        paymentRefundService.applyRefundSuccess(testPayment, refundAmount, refundAmount, "Full refund", testTicket);

        verify(paymentRepository, never()).save(any());
    }
}
