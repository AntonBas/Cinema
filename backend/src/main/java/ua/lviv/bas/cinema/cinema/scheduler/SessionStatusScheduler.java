package ua.lviv.bas.cinema.cinema.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.domain.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.cinema.repository.SessionRepository;

@Slf4j
@Component
public class SessionStatusScheduler {

	private final SessionRepository sessionRepository;
	private final TransactionTemplate transactionTemplate;

	public SessionStatusScheduler(SessionRepository sessionRepository, PlatformTransactionManager transactionManager) {
		this.sessionRepository = sessionRepository;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	@Scheduled(cron = "${scheduler.session-status.cron:0 */5 * * * *}")
	@CacheEvict(value = "sessions", allEntries = true)
	public void updateSessionStatuses() {
		log.debug("Starting scheduled session status update");
		LocalDateTime now = LocalDateTime.now();

		int startedCount = updateStatuses(sessionRepository.findSessionsToStart(now), CinemaSessionStatus.ONGOING);
		int completedCount = updateStatuses(sessionRepository.findSessionsToComplete(now),
				CinemaSessionStatus.COMPLETED);

		log.info("Session status update completed: {} started, {} completed", startedCount, completedCount);
	}

	private int updateStatuses(List<Session> sessions, CinemaSessionStatus newStatus) {
		int updatedCount = 0;
		for (Session session : sessions) {
			Long sessionId = session.getId();
			try {
				transactionTemplate.executeWithoutResult(status -> updateStatus(sessionId, newStatus));
				updatedCount++;
				log.info("Session {} transitioned to {}", sessionId, newStatus);
			} catch (ObjectOptimisticLockingFailureException e) {
				log.warn("Skipped updating session {} to {} due to concurrent update, will retry on next run",
						sessionId, newStatus);
			}
		}
		return updatedCount;
	}

	private void updateStatus(Long sessionId, CinemaSessionStatus newStatus) {
		sessionRepository.findById(sessionId).ifPresent(session -> {
			session.setStatus(newStatus);
			sessionRepository.save(session);
		});
	}
}
