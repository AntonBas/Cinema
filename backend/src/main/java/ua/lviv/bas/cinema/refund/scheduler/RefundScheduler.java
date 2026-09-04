package ua.lviv.bas.cinema.refund.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentProcessingException;
import ua.lviv.bas.cinema.payment.service.PaymentGatewayService;
import ua.lviv.bas.cinema.payment.service.RefundGatewayStatus;
import ua.lviv.bas.cinema.refund.domain.status.RefundStatus;
import ua.lviv.bas.cinema.refund.repository.RefundRepository;
import ua.lviv.bas.cinema.refund.repository.projection.StuckRefundProjection;
import ua.lviv.bas.cinema.refund.service.RefundTransactionExecutor;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundScheduler {

	private final RefundRepository refundRepository;
	private final RefundTransactionExecutor refundTransactionExecutor;
	private final PaymentGatewayService paymentGatewayService;

	@Scheduled(fixedRateString = "${scheduler.refund.reconciliation-interval:300000}")
	public void completeStuckRefunds() {
		LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
		var stuckRefunds = refundRepository.findStuckRefunds(RefundStatus.PROCESSING, cutoff);

		if (stuckRefunds.isEmpty()) {
			log.debug("No refunds stuck in PROCESSING");
			return;
		}

		log.info("Found {} refund(s) stuck in PROCESSING, attempting to reconcile with LiqPay", stuckRefunds.size());

		for (var stuck : stuckRefunds) {
			try {
				reconcile(stuck);
			} catch (Exception e) {
				log.error("Failed to reconcile refund {} (ticket {}), will retry on next run", stuck.getRefundId(),
						stuck.getTicketId(), e);
			}
		}
	}

	private void reconcile(StuckRefundProjection stuck) {
		var gatewayStatus = paymentGatewayService.checkRefundStatus(stuck.getLiqpayOrderId());

		switch (gatewayStatus) {
			case CONFIRMED -> {
				refundTransactionExecutor.applySuccess(stuck.getRefundId(), stuck.getTicketId());
				log.info("Reconciled refund {} (ticket {}) from PROCESSING to PROCESSED - confirmed by LiqPay",
						stuck.getRefundId(), stuck.getTicketId());
			}
			case NOT_CONFIRMED -> {
				refundTransactionExecutor.markFailed(stuck.getRefundId(),
						new PaymentProcessingException("LiqPay confirmed the refund did not succeed"));
				log.warn("Refund {} (ticket {}) marked REJECTED - LiqPay confirmed the refund did not succeed",
						stuck.getRefundId(), stuck.getTicketId());
			}
			case UNKNOWN -> log.warn(
					"Could not yet confirm refund {} (ticket {}) status with LiqPay, will retry on next run",
					stuck.getRefundId(), stuck.getTicketId());
			default -> throw new IllegalStateException("Unexpected LiqPay reconciliation status: " + gatewayStatus);
		}
	}
}
