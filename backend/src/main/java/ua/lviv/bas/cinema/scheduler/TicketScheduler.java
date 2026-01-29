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
		List<Ticket> activeTicketsForPastSessions = ticketRepository
				.findActiveTicketsWithPastSessions(TicketStatus.ACTIVE, now);

		if (activeTicketsForPastSessions.isEmpty()) {
			log.debug("No tickets to mark as used");
			return;
		}

		log.info("Found {} tickets to mark as used", activeTicketsForPastSessions.size());

		for (Ticket ticket : activeTicketsForPastSessions) {
			ticket.setStatus(TicketStatus.USED);
		}

		ticketRepository.saveAll(activeTicketsForPastSessions);
		log.info("Successfully marked {} tickets as used", activeTicketsForPastSessions.size());
	}

	@Scheduled(cron = "${scheduler.ticket.cleanup-cron:0 0 3 * * *}")
	@Transactional
	public void cleanupRefundedTickets() {
		log.debug("Starting refunded tickets cleanup");
		LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);

		List<Ticket> refundedTickets = ticketRepository
				.findByStatusInAndPurchaseTimeBefore(List.of(TicketStatus.REFUNDED), oneYearAgo);

		if (!refundedTickets.isEmpty()) {
			ticketRepository.deleteAll(refundedTickets);
			log.info("Cleaned up {} refunded tickets", refundedTickets.size());
		} else {
			log.debug("No refunded tickets to clean up");
		}
	}

	@Scheduled(cron = "${scheduler.ticket.daily-stats:0 0 22 * * *}")
	@Transactional(readOnly = true)
	public void generateDailyTicketStatistics() {
		log.debug("Starting daily ticket statistics generation");
		LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
		LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

		List<Ticket> todayTickets = ticketRepository.findAll().stream()
				.filter(ticket -> ticket.getPurchaseTime() != null && !ticket.getPurchaseTime().isBefore(startOfDay)
						&& !ticket.getPurchaseTime().isAfter(endOfDay))
				.toList();

		long totalTicketsToday = todayTickets.size();
		long activeTickets = todayTickets.stream().filter(t -> t.getStatus() == TicketStatus.ACTIVE).count();
		long usedTickets = todayTickets.stream().filter(t -> t.getStatus() == TicketStatus.USED).count();
		long refundedTickets = todayTickets.stream().filter(t -> t.getStatus() == TicketStatus.REFUNDED).count();

		log.info("Daily ticket statistics:");
		log.info("  Total tickets sold today: {}", totalTicketsToday);
		log.info("  Active tickets: {}", activeTickets);
		log.info("  Used tickets: {}", usedTickets);
		log.info("  Refunded tickets: {}", refundedTickets);
	}

	@Scheduled(cron = "${scheduler.ticket.upcoming-reminder:0 */15 * * * *}")
	@Transactional(readOnly = true)
	public void checkUpcomingSessionsForReminders() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime reminderTime = now.plusHours(1);
		LocalDateTime twoHoursBefore = now.plusHours(2);

		List<Ticket> ticketsForReminder = ticketRepository.findByBookingSessionStartTimeBetweenAndStatus(reminderTime,
				twoHoursBefore, TicketStatus.ACTIVE);

		if (!ticketsForReminder.isEmpty()) {
			log.info("Found {} active tickets with sessions starting soon (1-2 hours)", ticketsForReminder.size());
		}
	}
}