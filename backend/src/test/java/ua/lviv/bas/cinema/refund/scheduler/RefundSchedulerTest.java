package ua.lviv.bas.cinema.refund.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.refund.domain.status.RefundStatus;
import ua.lviv.bas.cinema.refund.repository.RefundRepository;
import ua.lviv.bas.cinema.refund.repository.projection.StuckRefundProjection;
import ua.lviv.bas.cinema.refund.service.RefundTransactionExecutor;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefundSchedulerTest {

    @Mock
    private RefundRepository refundRepository;
    @Mock
    private RefundTransactionExecutor refundTransactionExecutor;
    @Mock
    private StuckRefundProjection stuckRefund1;
    @Mock
    private StuckRefundProjection stuckRefund2;

    @InjectMocks
    private RefundScheduler refundScheduler;

    @Test
    void completeStuckRefundsWhenNoneFoundShouldDoNothing() {
        when(refundRepository.findStuckRefunds(eq(RefundStatus.PROCESSING), any(LocalDateTime.class)))
                .thenReturn(List.of());

        refundScheduler.completeStuckRefunds();

        verifyNoInteractions(refundTransactionExecutor);
    }

    @Test
    void completeStuckRefundsShouldFinalizeEachStuckRefundOnlyThroughExecutor() {
        when(stuckRefund1.getRefundId()).thenReturn(1L);
        when(stuckRefund1.getTicketId()).thenReturn(10L);
        when(stuckRefund2.getRefundId()).thenReturn(2L);
        when(stuckRefund2.getTicketId()).thenReturn(20L);

        when(refundRepository.findStuckRefunds(eq(RefundStatus.PROCESSING), any(LocalDateTime.class)))
                .thenReturn(List.of(stuckRefund1, stuckRefund2));

        refundScheduler.completeStuckRefunds();

        verify(refundTransactionExecutor).applySuccess(1L, 10L);
        verify(refundTransactionExecutor).applySuccess(2L, 20L);
        verifyNoMoreInteractions(refundTransactionExecutor);
    }

    @Test
    void completeStuckRefundsWhenOneFailsShouldStillProcessTheOthers() {
        when(stuckRefund1.getRefundId()).thenReturn(1L);
        when(stuckRefund1.getTicketId()).thenReturn(10L);
        when(stuckRefund2.getRefundId()).thenReturn(2L);
        when(stuckRefund2.getTicketId()).thenReturn(20L);

        when(refundRepository.findStuckRefunds(eq(RefundStatus.PROCESSING), any(LocalDateTime.class)))
                .thenReturn(List.of(stuckRefund1, stuckRefund2));
        doThrow(new RuntimeException("DB error")).when(refundTransactionExecutor).applySuccess(1L, 10L);

        refundScheduler.completeStuckRefunds();

        verify(refundTransactionExecutor).applySuccess(1L, 10L);
        verify(refundTransactionExecutor).applySuccess(2L, 20L);
    }
}
