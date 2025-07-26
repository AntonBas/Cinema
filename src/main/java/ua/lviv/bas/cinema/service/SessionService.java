package ua.lviv.bas.cinema.service;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dao.SessionRepository;
import ua.lviv.bas.cinema.domain.Session;

@Service
@RequiredArgsConstructor
public class SessionService {

	private static final Logger logger = LogManager.getLogger(SessionService.class);

	private final SessionRepository sessionRepository;

	public Session createSession(Session session) {
		logger.info("Creating Session: {}", session.getId());
		return sessionRepository.save(session);
	}

	public Session readSession(Long id) {
		logger.info("Reading Session with id: {}", id);
		return sessionRepository.findById(id).orElse(null);
	}

	public Session updateSession(Session session) {
		logger.info("Updating Session with id: {}", session.getId());
		return sessionRepository.save(session);
	}

	public void deleteSession(Long id) {
		logger.info("Deleting Session with id: {}", id);
		sessionRepository.deleteById(id);
	}

	public List<Session> getAllSessions() {
		logger.info("Retrieving all sessions");
		return sessionRepository.findAll();
	}

	public boolean isSessionTimeAvailable(LocalDateTime newStart, LocalDateTime newEnd, Long hallId,
			Long sessionIdToExclude) {
		List<Session> existingSessions = sessionRepository.findByHallId(hallId);

		for (Session session : existingSessions) {
			if (session.getId().equals(sessionIdToExclude))
				continue;

			LocalDateTime existingStart = session.getStartTime();
			LocalDateTime existingEnd = session.getEndTime();

			if (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)) {
				return false;
			}
		}
		return true;
	}

}
