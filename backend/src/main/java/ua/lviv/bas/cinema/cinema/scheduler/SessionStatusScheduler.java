package ua.lviv.bas.cinema.cinema.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.domain.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.cinema.repository.SessionRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionStatusScheduler {

	private final SessionRepository sessionRepository;

	@Scheduled(cron = "${scheduler.session-status.cron:0 */5 * * * *}")
	@CacheEvict(value = "sessions", allEntries = true)
	@Transactional
	public void updateSessionStatuses() {
		log.debug("Starting scheduled session status update");
		LocalDateTime now = LocalDateTime.now();

		int startedCount = updateStatuses(sessionRepository.findSessionsToStart(now), CinemaSessionStatus.SCHEDULED,
				CinemaSessionStatus.ONGOING);
		int completedCount = updateStatuses(sessionRepository.findSessionsToComplete(now),
				CinemaSessionStatus.ONGOING, CinemaSessionStatus.COMPLETED);

		log.info("Session status update completed: {} started, {} completed", startedCount, completedCount);
	}

	private int updateStatuses(List<Session> sessions, CinemaSessionStatus fromStatus, CinemaSessionStatus newStatus) {
		if (sessions.isEmpty()) {
			return 0;
		}
		List<Long> ids = sessions.stream().map(Session::getId).toList();
		int updatedCount = sessionRepository.updateStatusForIds(ids, fromStatus, newStatus);
		log.info("Transitioned {} session(s) to {}", updatedCount, newStatus);
		return updatedCount;
	}
}
