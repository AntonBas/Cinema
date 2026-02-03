package ua.lviv.bas.cinema.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.repository.TicketRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketScheduler {

	private final TicketRepository ticketRepository;

	@Scheduled(fixedRateString = "${scheduler.ticket.mark-as-used:60000}")
	@Transactional
	public void markTicketsAsUsedAfterSession() {
		log.debug("Starting to mark tickets as used after sessions");
		LocalDateTime now = LocalDateTime.now();
		List<Ticket> tickets = ticketRepository.findActiveTicketsWithPastSessions(TicketStatus.ACTIVE, now);

		if (tickets.isEmpty()) {
			log.debug("No tickets to mark as used");
			return;
		}

		tickets.forEach(ticket -> ticket.setStatus(TicketStatus.USED));
		ticketRepository.saveAll(tickets);
		log.info("Successfully marked {} tickets as used", tickets.size());
	}

	@Scheduled(cron = "${scheduler.ticket.cleanup-cron:0 0 3 * * *}")
	@Transactional
	public void cleanupRefundedTickets() {
		log.debug("Starting refunded tickets cleanup");
		LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
		List<TicketStatus> refundedStatus = List.of(TicketStatus.REFUNDED);

		List<Ticket> tickets = ticketRepository.findByStatusInAndPurchaseTimeBefore(refundedStatus, oneYearAgo);

		if (!tickets.isEmpty()) {
			ticketRepository.deleteAll(tickets);
			log.info("Cleaned up {} refunded tickets", tickets.size());
		} else {
			log.debug("No refunded tickets to clean up");
		}
	}

	@Scheduled(cron = "${scheduler.ticket.daily-stats:0 0 22 * * *}")
	@Transactional(readOnly = true)
	public void generateDailyTicketStatistics() {
		log.debug("Starting daily ticket statistics generation");

		long totalTicketsToday = ticketRepository.countToday();
		long activeTicketsToday = ticketRepository.countByStatusToday(TicketStatus.ACTIVE);
		long usedTicketsToday = ticketRepository.countByStatusToday(TicketStatus.USED);
		long refundedTicketsToday = ticketRepository.countByStatusToday(TicketStatus.REFUNDED);

		log.info("Daily ticket statistics:");
		log.info("  Total tickets sold today: {}", totalTicketsToday);
		log.info("  Active tickets today: {}", activeTicketsToday);
		log.info("  Used tickets today: {}", usedTicketsToday);
		log.info("  Refunded tickets today: {}", refundedTicketsToday);
	}

	@Scheduled(cron = "${scheduler.ticket.upcoming-reminder:0 */15 * * * *}")
	@Transactional(readOnly = true)
	public void checkUpcomingSessionsForReminders() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime fromTime = now.plusHours(1);
		LocalDateTime toTime = now.plusHours(2);

		List<Ticket> tickets = ticketRepository.findByBookingSessionStartTimeBetweenAndStatus(fromTime, toTime,
				TicketStatus.ACTIVE);

		if (!tickets.isEmpty()) {
			log.info("Found {} active tickets with sessions starting soon (1-2 hours)", tickets.size());
		}
	}
}