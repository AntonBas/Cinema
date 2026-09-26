package ua.lviv.bas.cinema.refund.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ua.lviv.bas.cinema.payment.service.PaymentGatewayService;
import ua.lviv.bas.cinema.payment.service.RefundGatewayStatus;
import ua.lviv.bas.cinema.refund.domain.status.RefundStatus;
import ua.lviv.bas.cinema.refund.repository.RefundRepository;
import ua.lviv.bas.cinema.refund.repository.projection.StuckRefundProjection;
import ua.lviv.bas.cinema.refund.service.RefundTransactionExecutor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RefundSchedulerTest {

    @Mock
    private RefundRepository refundRepository;
    @Mock
    private RefundTransactionExecutor refundTransactionExecutor;
    @Mock
    private PaymentGatewayService paymentGatewayService;
    @Mock
    private StuckRefundProjection stuckRefund1;
    @Mock
    private StuckRefundProjection stuckRefund2;

    @InjectMocks
    private RefundScheduler refundScheduler;

    @Test
    void completeStuckRefundsWhenNoneFoundShouldDoNothing() {
        when(refundRepository.findStuckRefunds(eq(RefundStatus.PROCESSING), any(Instant.class)))
                .thenReturn(List.of());

        refundScheduler.completeStuckRefunds();

        verifyNoInteractions(refundTransactionExecutor, paymentGatewayService);
    }

    @Test
    void completeStuckRefundsWhenGatewayConfirmsShouldFinalizeThroughExecutor() {
        when(stuckRefund1.getRefundId()).thenReturn(1L);
        when(stuckRefund1.getTicketId()).thenReturn(10L);
        when(stuckRefund1.getLiqpayOrderId()).thenReturn("ORD_1");
        stubAmounts(stuckRefund1);
        when(stuckRefund2.getRefundId()).thenReturn(2L);
        when(stuckRefund2.getTicketId()).thenReturn(20L);
        when(stuckRefund2.getLiqpayOrderId()).thenReturn("ORD_2");
        stubAmounts(stuckRefund2);

        when(refundRepository.findStuckRefunds(eq(RefundStatus.PROCESSING), any(Instant.class)))
                .thenReturn(List.of(stuckRefund1, stuckRefund2));
        when(paymentGatewayService.checkRefundStatus(eq("ORD_1"), any(), any())).thenReturn(RefundGatewayStatus.CONFIRMED);
        when(paymentGatewayService.checkRefundStatus(eq("ORD_2"), any(), any())).thenReturn(RefundGatewayStatus.CONFIRMED);

        refundScheduler.completeStuckRefunds();

        verify(refundTransactionExecutor).applySuccess(1L, 10L);
        verify(refundTransactionExecutor).applySuccess(2L, 20L);
        verify(refundTransactionExecutor, never()).markFailed(any(), any());
    }

    @Test
    void completeStuckRefundsWhenGatewayDeniesShouldMarkFailedInsteadOfApplySuccess() {
        when(stuckRefund1.getRefundId()).thenReturn(1L);
        when(stuckRefund1.getTicketId()).thenReturn(10L);
        when(stuckRefund1.getLiqpayOrderId()).thenReturn("ORD_1");
        stubAmounts(stuckRefund1);

        when(refundRepository.findStuckRefunds(eq(RefundStatus.PROCESSING), any(Instant.class)))
                .thenReturn(List.of(stuckRefund1));
        when(paymentGatewayService.checkRefundStatus(eq("ORD_1"), any(), any())).thenReturn(RefundGatewayStatus.NOT_CONFIRMED);

        refundScheduler.completeStuckRefunds();

        verify(refundTransactionExecutor, never()).applySuccess(any(), any());
        verify(refundTransactionExecutor).markFailed(eq(1L), any());
    }

    @Test
    void completeStuckRefundsWhenGatewayStatusUnknownShouldLeaveRefundUntouched() {
        when(stuckRefund1.getRefundId()).thenReturn(1L);
        when(stuckRefund1.getTicketId()).thenReturn(10L);
        when(stuckRefund1.getLiqpayOrderId()).thenReturn("ORD_1");
        stubAmounts(stuckRefund1);

        when(refundRepository.findStuckRefunds(eq(RefundStatus.PROCESSING), any(Instant.class)))
                .thenReturn(List.of(stuckRefund1));
        when(paymentGatewayService.checkRefundStatus(eq("ORD_1"), any(), any())).thenReturn(RefundGatewayStatus.UNKNOWN);

        refundScheduler.completeStuckRefunds();

        verifyNoInteractions(refundTransactionExecutor);
    }

    @Test
    void completeStuckRefundsWhenOneFailsShouldStillProcessTheOthers() {
        when(stuckRefund1.getRefundId()).thenReturn(1L);
        when(stuckRefund1.getTicketId()).thenReturn(10L);
        when(stuckRefund1.getLiqpayOrderId()).thenReturn("ORD_1");
        stubAmounts(stuckRefund1);
        when(stuckRefund2.getRefundId()).thenReturn(2L);
        when(stuckRefund2.getTicketId()).thenReturn(20L);
        when(stuckRefund2.getLiqpayOrderId()).thenReturn("ORD_2");
        stubAmounts(stuckRefund2);

        when(refundRepository.findStuckRefunds(eq(RefundStatus.PROCESSING), any(Instant.class)))
                .thenReturn(List.of(stuckRefund1, stuckRefund2));
        when(paymentGatewayService.checkRefundStatus(eq("ORD_1"), any(), any())).thenReturn(RefundGatewayStatus.CONFIRMED);
        when(paymentGatewayService.checkRefundStatus(eq("ORD_2"), any(), any())).thenReturn(RefundGatewayStatus.CONFIRMED);
        doThrow(new RuntimeException("DB error")).when(refundTransactionExecutor).applySuccess(1L, 10L);

        refundScheduler.completeStuckRefunds();

        verify(refundTransactionExecutor).applySuccess(1L, 10L);
        verify(refundTransactionExecutor).applySuccess(2L, 20L);
    }

    @Test
    void completeStuckRefundsShouldExpectAlreadyProcessedRefundsPlusThisOne() {
        when(stuckRefund1.getRefundId()).thenReturn(1L);
        when(stuckRefund1.getTicketId()).thenReturn(10L);
        when(stuckRefund1.getLiqpayOrderId()).thenReturn("ORD_1");
        when(stuckRefund1.getPaymentId()).thenReturn(5L);
        when(stuckRefund1.getRefundAmount()).thenReturn(new BigDecimal("40.00"));
        when(stuckRefund1.getPaymentAmount()).thenReturn(new BigDecimal("200.00"));

        when(refundRepository.findStuckRefunds(eq(RefundStatus.PROCESSING), any(Instant.class)))
                .thenReturn(List.of(stuckRefund1));
        when(refundRepository.sumAmountByPaymentIdAndStatus(5L, RefundStatus.PROCESSED))
                .thenReturn(new BigDecimal("60.00"));
        when(paymentGatewayService.checkRefundStatus("ORD_1", new BigDecimal("100.00"), new BigDecimal("200.00")))
                .thenReturn(RefundGatewayStatus.CONFIRMED);

        refundScheduler.completeStuckRefunds();

        verify(refundTransactionExecutor).applySuccess(1L, 10L);
    }

    @Test
    void completeStuckRefundsWhenUnconfirmedForTooLongShouldStillLeaveRefundUntouched() {
        ReflectionTestUtils.setField(refundScheduler, "manualReviewAfterHours", 24);
        when(stuckRefund1.getLiqpayOrderId()).thenReturn("ORD_1");
        when(stuckRefund1.getCreatedDate()).thenReturn(Instant.now().minus(Duration.ofDays(2)));
        stubAmounts(stuckRefund1);

        when(refundRepository.findStuckRefunds(eq(RefundStatus.PROCESSING), any(Instant.class)))
                .thenReturn(List.of(stuckRefund1));
        when(paymentGatewayService.checkRefundStatus(eq("ORD_1"), any(), any()))
                .thenReturn(RefundGatewayStatus.UNKNOWN);

        refundScheduler.completeStuckRefunds();

        verifyNoInteractions(refundTransactionExecutor);
    }

    private void stubAmounts(StuckRefundProjection stuck) {
        lenient().when(stuck.getRefundAmount()).thenReturn(new BigDecimal("50.00"));
        lenient().when(stuck.getPaymentAmount()).thenReturn(new BigDecimal("100.00"));
        lenient().when(stuck.getCreatedDate()).thenReturn(Instant.now().minus(Duration.ofMinutes(10)));
        lenient().when(refundRepository.sumAmountByPaymentIdAndStatus(any(), eq(RefundStatus.PROCESSED)))
                .thenReturn(BigDecimal.ZERO);
    }
}
