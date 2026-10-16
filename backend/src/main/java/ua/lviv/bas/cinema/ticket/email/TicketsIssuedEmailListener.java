package ua.lviv.bas.cinema.ticket.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ua.lviv.bas.cinema.config.async.AsyncConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketsIssuedEmailListener {

    private final TicketsEmailService ticketsEmailService;

    @Async(AsyncConfig.EMAIL_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTicketsIssued(TicketsIssuedEvent event) {
        try {
            ticketsEmailService.sendAndMarkSent(event.bookingId());
        } catch (Exception e) {
            log.error("Failed to send tickets email for booking {}, TicketsEmailRetryScheduler will retry",
                    event.bookingId(), e);
        }
    }
}
