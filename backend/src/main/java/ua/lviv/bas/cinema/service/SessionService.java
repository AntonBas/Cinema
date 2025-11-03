package ua.lviv.bas.cinema.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.dto.SessionDto;
import ua.lviv.bas.cinema.dto.SessionRequest;
import ua.lviv.bas.cinema.exception.ConflictException;
import ua.lviv.bas.cinema.exception.SessionNotFoundException;
import ua.lviv.bas.cinema.mapper.SessionMapper;
import ua.lviv.bas.cinema.repository.SessionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

	private final SessionRepository sessionRepository;
	private final SessionMapper sessionMapper;
	private final MovieService movieService;
	private final CinemaHallService cinemaHallService;

	@Transactional
	public SessionDto createSession(SessionRequest request) {
		log.info("Creating session: movieId={}, hallId={}, startTime={}", request.getMovieId(), request.getHallId(),
				request.getStartTime());

		if (!request.isStartTimeValid()) {
			throw new IllegalArgumentException("Session must start at least 30 minutes from now");
		}

		Movie movie = movieService.getMovieEntityById(request.getMovieId());
		CinemaHall hall = cinemaHallService.getHallEntityById(request.getHallId());

		if (hasTimeConflict(hall.getId(), request.getStartTime(), movie.getDurationMinutes(), null)) {
			throw new ConflictException("Time conflict: there is already a session in this hall at the selected time");
		}

		Session session = sessionMapper.toEntity(request);
		session.setMovie(movie);
		session.setHall(hall);

		Session saved = sessionRepository.save(session);
		log.info("Session created successfully: id={}", saved.getId());

		return sessionMapper.toDto(saved);
	}

	@Transactional
	public SessionDto getSessionById(Long id) {
		log.debug("Retrieving session by id: {}", id);
		Session session = sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(id));
		return sessionMapper.toDto(session);
	}

	@Transactional
	public SessionDto updateSession(Long id, SessionRequest request) {
		log.info("Updating session: id={}", id);

		Session session = sessionRepository.findById(id).orElseThrow(() -> new SessionNotFoundException(id));

		if (!request.isStartTimeValid()) {
			throw new IllegalArgumentException("Session must start at least 30 minutes from now");
		}

		Movie movie = movieService.getMovieEntityById(request.getMovieId());
		CinemaHall hall = cinemaHallService.getHallEntityById(request.getHallId());

		if (hasTimeConflict(hall.getId(), request.getStartTime(), movie.getDurationMinutes(), id)) {
			throw new ConflictException("Time conflict: there is already a session in this hall at the selected time");
		}

		session.setStartTime(request.getStartTime());
		session.setPrice(request.getPrice());
		session.setMovie(movie);
		session.setHall(hall);

		Session updated = sessionRepository.save(session);
		log.info("Session updated successfully: id={}", id);

		return sessionMapper.toDto(updated);
	}

	@Transactional
	public void deleteSession(Long id) {
		log.info("Deleting session: id={}", id);

		if (!sessionRepository.existsById(id)) {
			throw new SessionNotFoundException(id);
		}

		sessionRepository.deleteById(id);
		log.info("Session deleted successfully: id={}", id);
	}

	@Transactional(readOnly = true)
	public List<SessionDto> getAllSession() {
		log.debug("Retrieving all sessions");
		return sessionRepository.findAll().stream().map(sessionMapper::toDto).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<SessionDto> getSessionsByDate(LocalDate date) {
		log.debug("Retrieving sessions for date: {}", date);
		LocalDateTime startOfDay = date.atStartOfDay();
		LocalDateTime endOfDay = date.atTime(23, 59, 59);

		return sessionRepository.findByStartTimeBetween(startOfDay, endOfDay).stream().map(sessionMapper::toDto)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<SessionDto> getSessionsByHall(Long hallId) {
		log.debug("Retrieving sessions for hall: {}", hallId);
		return sessionRepository.findByHallId(hallId).stream().map(sessionMapper::toDto).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<SessionDto> getSessionsByMovie(Long movieId) {
		log.debug("Retrieving sessions for movie: {}", movieId);
		return sessionRepository.findByMovieId(movieId).stream().map(sessionMapper::toDto).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public boolean hasTimeConflict(Long hallId, LocalDateTime startTime, Integer durationMinutes,
			Long excludeSessionId) {
		LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

		List<Session> conflictingSessions = sessionRepository.findConflictingSessions(hallId, startTime, endTime,
				excludeSessionId);

		return !conflictingSessions.isEmpty();
	}

	@Transactional(readOnly = true)
	public List<SessionDto> getAvailableSessions() {
		log.debug("Retrieving available sessions");
		return sessionRepository.findByStartTimeAfter(LocalDateTime.now()).stream().filter(Session::isAvailable)
				.map(sessionMapper::toDto).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<SessionDto> getUpcomingSessions(int days) {
		log.debug("Retrieving upcoming sessions for next {} days", days);
		LocalDateTime start = LocalDateTime.now();
		LocalDateTime end = start.plusDays(days);

		return sessionRepository.findByStartTimeBetween(start, end).stream().map(sessionMapper::toDto)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<SessionDto> getTodaySessions() {
		log.debug("Retrieving today's sessions");
		return getSessionsByDate(LocalDate.now());
	}
}
