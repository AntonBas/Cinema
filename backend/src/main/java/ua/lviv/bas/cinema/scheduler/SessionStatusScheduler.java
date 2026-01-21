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

		List<Session> sessionsToStart = sessionRepository.findSessionsToStart(now, CinemaSessionStatus.SCHEDULED);
		if (!sessionsToStart.isEmpty()) {
			sessionsToStart.forEach(session -> {
				session.setStatus(CinemaSessionStatus.ONGOING);
				log.info("Session {} started", session.getId());
			});
			sessionRepository.saveAll(sessionsToStart);
			log.debug("Updated {} sessions to ONGOING status", sessionsToStart.size());
		}

		List<Session> sessionsToComplete = sessionRepository.findSessionsToComplete(now);
		if (!sessionsToComplete.isEmpty()) {
			sessionsToComplete.forEach(session -> {
				session.setStatus(CinemaSessionStatus.COMPLETED);
				log.info("Session {} completed", session.getId());
			});
			sessionRepository.saveAll(sessionsToComplete);
			log.debug("Updated {} sessions to COMPLETED status", sessionsToComplete.size());
		}

		log.info("Session status update completed: {} started, {} completed", sessionsToStart.size(),
				sessionsToComplete.size());
	}
}