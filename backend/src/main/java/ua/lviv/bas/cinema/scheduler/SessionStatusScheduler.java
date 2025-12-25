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

	@Scheduled(cron = "0 */5 * * * *")
	@Transactional
	public void updateSessionStatuses() {
		log.debug("Starting scheduled session status update");
		LocalDateTime now = LocalDateTime.now();

		updateToOngoing(now);
		updateToCompleted(now);

		log.debug("Completed scheduled session status update");
	}

	private void updateToOngoing(LocalDateTime now) {
		List<Session> sessionsToStart = sessionRepository.findByStatusAndStartTimeBefore(CinemaSessionStatus.SCHEDULED,
				now);

		if (!sessionsToStart.isEmpty()) {
			log.info("Found {} sessions to mark as ONGOING", sessionsToStart.size());

			sessionsToStart.forEach(session -> {
				session.setStatus(CinemaSessionStatus.ONGOING);
				log.debug("Marked session {} as ONGOING", session.getId());
			});

			sessionRepository.saveAll(sessionsToStart);
		}
	}

	private void updateToCompleted(LocalDateTime now) {
		List<Session> sessionsToComplete = sessionRepository
				.findByStatusAndEndTimeBefore(CinemaSessionStatus.ONGOING.toString(), now);

		if (!sessionsToComplete.isEmpty()) {
			log.info("Found {} sessions to mark as COMPLETED", sessionsToComplete.size());

			sessionsToComplete.forEach(session -> {
				session.setStatus(CinemaSessionStatus.COMPLETED);
				log.debug("Marked session {} as COMPLETED", session.getId());
			});

			sessionRepository.saveAll(sessionsToComplete);
		}
	}
}