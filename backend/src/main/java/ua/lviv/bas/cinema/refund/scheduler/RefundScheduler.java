package ua.lviv.bas.cinema.refund.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.refund.domain.status.RefundStatus;
import ua.lviv.bas.cinema.refund.repository.RefundRepository;
import ua.lviv.bas.cinema.refund.service.RefundTransactionExecutor;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundScheduler {

	private final RefundRepository refundRepository;
	private final RefundTransactionExecutor refundTransactionExecutor;

	@Scheduled(fixedRateString = "${scheduler.refund.reconciliation-interval:300000}")
	public void completeStuckRefunds() {
		LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
		var stuckRefunds = refundRepository.findStuckRefunds(RefundStatus.PROCESSING, cutoff);

		if (stuckRefunds.isEmpty()) {
			log.debug("No refunds stuck in PROCESSING");
			return;
		}

		log.info("Found {} refund(s) stuck in PROCESSING, attempting to finalize", stuckRefunds.size());

		for (var stuck : stuckRefunds) {
			try {
				refundTransactionExecutor.applySuccess(stuck.getRefundId(), stuck.getTicketId());
				log.info("Reconciled refund {} (ticket {}) from PROCESSING to PROCESSED", stuck.getRefundId(),
						stuck.getTicketId());
			} catch (Exception e) {
				log.error("Failed to reconcile refund {} (ticket {}), will retry on next run", stuck.getRefundId(),
						stuck.getTicketId(), e);
			}
		}
	}
}
