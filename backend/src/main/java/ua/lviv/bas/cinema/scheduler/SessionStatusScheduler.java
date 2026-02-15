package ua.lviv.bas.cinema.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.repository.SessionRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionStatusScheduler {

	private final SessionRepository sessionRepository;

	@Scheduled(cron = "${scheduler.session-status.cron:0 */5 * * * *}")
	@Transactional
	public void updateSessionStatuses() {
		log.debug("Starting scheduled session status update");
		LocalDateTime now = LocalDateTime.now();

		List<Session> sessionsToStart = sessionRepository.findSessionsToStart(now);
		int startedCount = 0;
		for (Session session : sessionsToStart) {
			try {
				session.setStatus(CinemaSessionStatus.ONGOING);
				startedCount++;
				log.info("Session {} started", session.getId());
			} catch (Exception e) {
				log.error("Failed to start session {}: {}", session.getId(), e.getMessage());
			}
		}
		if (!sessionsToStart.isEmpty()) {
			sessionRepository.saveAll(sessionsToStart);
		}

		List<Session> sessionsToComplete = sessionRepository.findSessionsToComplete(now);
		int completedCount = 0;
		for (Session session : sessionsToComplete) {
			try {
				session.setStatus(CinemaSessionStatus.COMPLETED);
				completedCount++;
				log.info("Session {} completed", session.getId());
			} catch (Exception e) {
				log.error("Failed to complete session {}: {}", session.getId(), e.getMessage());
			}
		}
		if (!sessionsToComplete.isEmpty()) {
			sessionRepository.saveAll(sessionsToComplete);
		}

		log.info("Session status update completed: {} started, {} completed", startedCount, completedCount);
	}
}