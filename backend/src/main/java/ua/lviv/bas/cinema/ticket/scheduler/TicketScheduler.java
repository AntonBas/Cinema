package ua.lviv.bas.cinema.ticket.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.cinema.domain.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.ticket.domain.Ticket;
import ua.lviv.bas.cinema.ticket.domain.TicketStatus;
import ua.lviv.bas.cinema.ticket.repository.TicketRepository;
import ua.lviv.bas.cinema.ticket.repository.specification.TicketSpecification;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketScheduler {

    private final TicketRepository ticketRepository;
    private final TicketSpecification ticketSpecification;

    @Scheduled(fixedRateString = "${scheduler.ticket.mark-as-used:60000}")
    @Caching(evict = {
            @CacheEvict(value = "ticket", allEntries = true),
            @CacheEvict(value = "ticketList", allEntries = true)
    })
    @Transactional
    public void markTicketsAsExpiredAfterSession() {
        log.debug("Starting to mark tickets as expired after sessions");

        Specification<Ticket> spec = Specification
                .where(ticketSpecification.hasStatus(TicketStatus.ACTIVE))
                .and(ticketSpecification.hasSessionStatus(CinemaSessionStatus.COMPLETED));

        List<Long> ticketIds = ticketRepository.findAll(spec).stream().map(Ticket::getId).toList();

        if (ticketIds.isEmpty()) {
            log.debug("No tickets to mark as expired");
            return;
        }

        int expiredCount = ticketRepository.updateStatusIfCurrentForIds(ticketIds, TicketStatus.ACTIVE,
                TicketStatus.EXPIRED);
        log.info("Successfully marked {} of {} tickets as expired", expiredCount, ticketIds.size());
    }
}
