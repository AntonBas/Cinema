package ua.lviv.bas.cinema.refund.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.cinema.domain.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.refund.domain.status.RefundStatus;
import ua.lviv.bas.cinema.refund.dto.request.RefundRequest;
import ua.lviv.bas.cinema.refund.repository.RefundRepository;
import ua.lviv.bas.cinema.refund.service.RefundService;
import ua.lviv.bas.cinema.ticket.domain.TicketStatus;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancelledSessionRefundScheduler {

    private static final String REFUND_REASON = "Session cancelled by the cinema";
    private static final List<RefundStatus> BLOCKING_REFUND_STATUSES = List.of(RefundStatus.PROCESSING,
            RefundStatus.REJECTED);

    private final RefundRepository refundRepository;
    private final RefundService refundService;

    @Scheduled(fixedRateString = "${scheduler.refund.cancelled-session-interval:120000}")
    public void refundTicketsOfCancelledSessions() {
        var candidates = refundRepository.findRefundCandidates(TicketStatus.ACTIVE, CinemaSessionStatus.CANCELLED,
                BLOCKING_REFUND_STATUSES);

        if (candidates.isEmpty()) {
            log.debug("No tickets of cancelled sessions left to refund");
            return;
        }

        log.info("Refunding {} ticket(s) of cancelled sessions", candidates.size());

        for (var candidate : candidates) {
            try {
                refundService.refund(new RefundRequest(candidate.getTicketId(), REFUND_REASON), candidate.getUserId());
            } catch (Exception e) {
                log.error("Failed to refund ticket {} of a cancelled session, will retry on next run",
                        candidate.getTicketId(), e);
            }
        }
    }
}
