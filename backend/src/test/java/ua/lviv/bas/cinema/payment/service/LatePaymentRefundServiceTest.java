package ua.lviv.bas.cinema.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import ua.lviv.bas.cinema.audit.domain.AuditAction;
import ua.lviv.bas.cinema.audit.service.AuditService;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.common.DateTimeFormatterService;
import ua.lviv.bas.cinema.common.NumberGeneratorService;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentGatewayUnavailableException;
import ua.lviv.bas.cinema.notification.EmailService;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.payment.repository.PaymentRepository;
import ua.lviv.bas.cinema.support.CinemaTestFixtures;
import ua.lviv.bas.cinema.user.domain.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LatePaymentRefundServiceTest {

    private static final Long PAYMENT_ID = 7L;
    private static final BigDecimal AMOUNT = new BigDecimal("150.00");

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentGatewayService paymentGatewayService;
    @Mock
    private PaymentRefundService paymentRefundService;
    @Mock
    private AuditService auditService;
    @Mock
    private EmailService emailService;
    @Mock
    private DateTimeFormatterService dateTimeFormatter;
    @Mock
    private NumberGeneratorService numberGenerator;
    @Mock
    private PlatformTransactionManager transactionManager;

    private LatePaymentRefundService service;
    private Payment payment;

    @BeforeEach
    void setUp() {
        service = new LatePaymentRefundService(paymentRepository, paymentGatewayService, paymentRefundService,
                auditService, emailService, dateTimeFormatter, numberGenerator, transactionManager);

        var session = CinemaTestFixtures.session(CinemaTestFixtures.movie(), CinemaTestFixtures.hall());
        var booking = Booking.builder().id(3L).user(User.builder().id(1L).email("late@test.com").build())
                .session(session).status(BookingStatus.EXPIRED).build();
        payment = Payment.builder().id(PAYMENT_ID).booking(booking).amount(AMOUNT)
                .status(PaymentStatus.REFUND_REQUIRED).liqpayOrderId("ORD_7").liqpayPaymentId("LP_7").build();
    }

    @Test
    void refundShouldCallGatewayAndMarkPaymentRefunded() {
        when(paymentRepository.findByIdWithDetails(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentGatewayService.checkRefundStatus("ORD_7")).thenReturn(RefundGatewayStatus.UNKNOWN);
        when(paymentRepository.updateStatusIfCurrentIn(PAYMENT_ID, List.of(PaymentStatus.REFUND_REQUIRED),
                PaymentStatus.REFUNDED)).thenReturn(1);

        service.refund(PAYMENT_ID);

        verify(paymentRefundService).callLiqPayRefund(eq("LP_7"), eq("ORD_7"), eq(AMOUNT), anyString());
        verify(auditService).logChange(eq("Payment"), eq(PAYMENT_ID), anyString(), eq(AuditAction.REFUND), any(),
                any());
        verify(emailService).sendSafely(anyString(), eq(3L), any());
    }

    @Test
    void refundWhenGatewayAlreadyReversedShouldOnlyMarkRefunded() {
        when(paymentRepository.findByIdWithDetails(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentGatewayService.checkRefundStatus("ORD_7")).thenReturn(RefundGatewayStatus.CONFIRMED);
        when(paymentRepository.updateStatusIfCurrentIn(PAYMENT_ID, List.of(PaymentStatus.REFUND_REQUIRED),
                PaymentStatus.REFUNDED)).thenReturn(1);

        service.refund(PAYMENT_ID);

        verifyNoInteractions(paymentRefundService);
        verify(paymentRepository).updateStatusIfCurrentIn(PAYMENT_ID, List.of(PaymentStatus.REFUND_REQUIRED),
                PaymentStatus.REFUNDED);
    }

    @Test
    void refundWhenGatewayFailsShouldKeepRefundRequired() {
        when(paymentRepository.findByIdWithDetails(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentGatewayService.checkRefundStatus("ORD_7")).thenReturn(RefundGatewayStatus.UNKNOWN);
        doThrow(new PaymentGatewayUnavailableException("timeout", null))
                .when(paymentRefundService).callLiqPayRefund(any(), any(), any(), any());

        service.refund(PAYMENT_ID);

        verify(paymentRepository, never()).updateStatusIfCurrentIn(any(), any(), any());
        verifyNoInteractions(auditService, emailService);
    }

    @Test
    void refundWhenPaymentNoLongerRequiresRefundShouldDoNothing() {
        payment.setStatus(PaymentStatus.REFUNDED);
        when(paymentRepository.findByIdWithDetails(PAYMENT_ID)).thenReturn(Optional.of(payment));

        service.refund(PAYMENT_ID);

        verifyNoInteractions(paymentGatewayService, paymentRefundService, auditService, emailService);
    }
}
