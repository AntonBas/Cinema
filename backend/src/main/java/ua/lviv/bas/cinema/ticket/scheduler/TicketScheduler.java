package ua.lviv.bas.cinema.ticket.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ua.lviv.bas.cinema.cinema.domain.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.ticket.domain.Ticket;
import ua.lviv.bas.cinema.ticket.domain.TicketStatus;
import ua.lviv.bas.cinema.ticket.repository.TicketRepository;
import ua.lviv.bas.cinema.ticket.repository.specification.TicketSpecification;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class TicketScheduler {

    private final TicketRepository ticketRepository;
    private final TicketSpecification ticketSpecification;
    private final TransactionTemplate transactionTemplate;

    public TicketScheduler(TicketRepository ticketRepository, TicketSpecification ticketSpecification,
            PlatformTransactionManager transactionManager) {
        this.ticketRepository = ticketRepository;
        this.ticketSpecification = ticketSpecification;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Scheduled(fixedRateString = "${scheduler.ticket.mark-as-used:60000}")
    @CacheEvict(value = "tickets", allEntries = true)
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

        int expiredCount = 0;
        for (Ticket ticket : tickets) {
            Long ticketId = ticket.getId();
            try {
                transactionTemplate.executeWithoutResult(status -> markExpired(ticketId));
                expiredCount++;
            } catch (RuntimeException e) {
                log.error("Failed to mark ticket {} as expired, will retry on next run", ticketId, e);
            }
        }

        log.info("Successfully marked {} of {} tickets as expired", expiredCount, tickets.size());
    }

    private void markExpired(Long ticketId) {
        ticketRepository.findById(ticketId).ifPresent(ticket -> {
            ticket.setStatus(TicketStatus.EXPIRED);
            ticketRepository.save(ticket);
        });
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