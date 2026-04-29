package ua.lviv.bas.cinema.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.repository.ticket.TicketRepository;
import ua.lviv.bas.cinema.repository.ticket.specification.TicketSpecification;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketScheduler {

    private final TicketRepository ticketRepository;
    private final TicketSpecification ticketSpecification;

    @Scheduled(fixedRateString = "${scheduler.ticket.mark-as-used:60000}")
    @Transactional
    public void markTicketsAsExpiredAfterSession() {
        log.debug("Starting to mark tickets as expired after sessions");

        Specification<Ticket> spec = Specification
                .where(ticketSpecification.hasStatus(TicketStatus.ACTIVE))
                .and(ticketSpecification.hasSessionStatus(CinemaSessionStatus.COMPLETED));

        List<Ticket> tickets = ticketRepository.findAll(spec);

        if (tickets.isEmpty()) {
            log.debug("No tickets to mark as expired");
            return;
        }

        tickets.forEach(ticket -> ticket.setStatus(TicketStatus.EXPIRED));
        ticketRepository.saveAll(tickets);
        log.info("Successfully marked {} tickets as expired", tickets.size());
    }

    @Scheduled(cron = "${scheduler.ticket.cleanup-cron:0 0 3 * * *}")
    @Transactional
    public void cleanupRefundedTickets() {
        log.debug("Starting refunded tickets cleanup");
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);

        Specification<Ticket> spec = Specification
                .where(ticketSpecification.hasStatus(TicketStatus.REFUNDED))
                .and(ticketSpecification.purchaseTimeBefore(oneYearAgo));

        List<Ticket> tickets = ticketRepository.findAll(spec);

        if (!tickets.isEmpty()) {
            ticketRepository.deleteAll(tickets);
            log.info("Cleaned up {} refunded tickets", tickets.size());
        } else {
            log.debug("No refunded tickets to clean up");
        }
    }
}