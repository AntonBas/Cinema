package ua.lviv.bas.cinema.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.service.common.SessionService;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionStatusScheduler {

	private final SessionService sessionService;
	private final SessionRepository sessionRepository;

	@Scheduled(cron = "0 */5 * * * *")
	@Transactional
	public void updateSessionStatuses() {
		log.info("Starting scheduled session status update");

		List<Session> sessionsToStart = sessionService.findSessionsToStart();
		if (!sessionsToStart.isEmpty()) {
			sessionsToStart.forEach(session -> {
				session.setStatus(CinemaSessionStatus.ONGOING);
				log.info("Marked session {} as ONGOING (start time: {})", session.getId(), session.getStartTime());
			});
			sessionRepository.saveAll(sessionsToStart);
		}

		List<Session> sessionsToComplete = sessionService.findSessionsToComplete();
		if (!sessionsToComplete.isEmpty()) {
			sessionsToComplete.forEach(session -> {
				session.setStatus(CinemaSessionStatus.COMPLETED);
				log.info("Marked session {} as COMPLETED", session.getId());
			});
			sessionRepository.saveAll(sessionsToComplete);
		}

		log.info("Session status update completed: {} started, {} completed", sessionsToStart.size(),
				sessionsToComplete.size());
	}
}