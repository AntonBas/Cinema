package ua.lviv.bas.cinema.ticket.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ua.lviv.bas.cinema.booking.BookingStatus;
import ua.lviv.bas.cinema.booking.BookingRepository;
import ua.lviv.bas.cinema.ticket.email.TicketsEmailService;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketsEmailRetryScheduler {

    private static final Duration LISTENER_GRACE_PERIOD = Duration.ofMinutes(2);

    private final BookingRepository bookingRepository;
    private final TicketsEmailService ticketsEmailService;

    @Value("${scheduler.tickets-email.retry-interval:300000}")
    private long retryIntervalMillis;

    @Value("${scheduler.tickets-email.give-up-after-hours:24}")
    private int giveUpAfterHours;

    @Scheduled(fixedRateString = "${scheduler.tickets-email.retry-interval:300000}")
    public void retryUnsentTicketsEmails() {
        var now = Instant.now();
        var giveUpCutoff = now.minus(Duration.ofHours(giveUpAfterHours));
        reportAbandoned(giveUpCutoff);

        var bookingIds = bookingRepository.findIdsAwaitingTicketsEmail(BookingStatus.CONFIRMED, giveUpCutoff,
                now.minus(LISTENER_GRACE_PERIOD));
        if (bookingIds.isEmpty()) {
            log.debug("No confirmed bookings awaiting a tickets email");
            return;
        }

        log.info("Retrying tickets email for {} confirmed booking(s)", bookingIds.size());
        for (var bookingId : bookingIds) {
            try {
                ticketsEmailService.sendAndMarkSent(bookingId);
            } catch (Exception e) {
                log.warn("Retry of tickets email for booking {} failed, will retry on next run", bookingId, e);
            }
        }
    }

    private void reportAbandoned(Instant giveUpCutoff) {
        var abandonedIds = bookingRepository.findIdsAwaitingTicketsEmail(BookingStatus.CONFIRMED,
                giveUpCutoff.minusMillis(retryIntervalMillis), giveUpCutoff);
        if (!abandonedIds.isEmpty()) {
            log.error("Tickets email for booking(s) {} still not sent after {} hours - giving up retries, "
                    + "manual review is required", abandonedIds, giveUpAfterHours);
        }
    }
}
