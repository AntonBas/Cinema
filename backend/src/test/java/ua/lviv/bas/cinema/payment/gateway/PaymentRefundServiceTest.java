package ua.lviv.bas.cinema.payment.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.booking.Booking;
import ua.lviv.bas.cinema.payment.Payment;
import ua.lviv.bas.cinema.payment.PaymentStatus;
import ua.lviv.bas.cinema.user.User;
import ua.lviv.bas.cinema.payment.exception.PaymentProcessingException;
import ua.lviv.bas.cinema.payment.PaymentRepository;
import ua.lviv.bas.cinema.audit.AuditService;
import ua.lviv.bas.cinema.support.CinemaTestFixtures;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class PaymentRefundServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentGatewayService paymentGatewayService;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private PaymentRefundService paymentRefundService;

    private User testUser;
    private Booking testBooking;
    private Payment testPayment;

    private static final BigDecimal AMOUNT = new BigDecimal("200.00");

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("test@example.com").build();

        var movie = CinemaTestFixtures.movie();
        var hall = CinemaTestFixtures.hall();
        var session = CinemaTestFixtures.session(movie, hall);

        testBooking = Booking.builder().id(2L).user(testUser).session(session).finalPrice(AMOUNT).build();

        testPayment = Payment.builder().id(3L).booking(testBooking).amount(AMOUNT).status(PaymentStatus.PENDING)
                .liqpayOrderId("ORD_TEST123456789").build();
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

        paymentRefundService.applyRefundSuccess(testPayment, refundAmount, refundAmount, description);

        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.PARTIALLY_REFUNDED);
        verify(paymentRepository).save(testPayment);
    }

    @Test
    void applyRefundSuccessWhenFullRefundShouldMarkAsFullyRefunded() {
        testPayment.setStatus(PaymentStatus.SUCCESS);
        BigDecimal refundAmount = AMOUNT;
        String description = "Full refund";

        when(paymentRepository.save(testPayment)).thenReturn(testPayment);

        paymentRefundService.applyRefundSuccess(testPayment, refundAmount, refundAmount, description);

        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void applyRefundSuccessWhenCumulativeRefundsCoverFullAmountShouldMarkAsFullyRefunded() {
        testPayment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
        BigDecimal refundAmount = AMOUNT.subtract(new BigDecimal("50.00"));
        String description = "Second refund for multi-ticket booking";

        when(paymentRepository.save(testPayment)).thenReturn(testPayment);

        paymentRefundService.applyRefundSuccess(testPayment, refundAmount, AMOUNT, description);

        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void applyRefundSuccessWhenSecondPartialRefundKeepsSameStatusShouldStillUpdate() {
        testPayment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
        BigDecimal secondRefundAmount = new BigDecimal("50.00");
        BigDecimal totalRefundedAmount = new BigDecimal("150.00");

        when(paymentRepository.save(testPayment)).thenReturn(testPayment);

        paymentRefundService.applyRefundSuccess(testPayment, secondRefundAmount, totalRefundedAmount,
                "Second partial refund");

        assertThat(testPayment.getStatus()).isEqualTo(PaymentStatus.PARTIALLY_REFUNDED);
        verify(paymentRepository).save(testPayment);
        verify(auditService).logChange(anyString(), anyLong(), anyString(), any(), any(), any());
    }
}
