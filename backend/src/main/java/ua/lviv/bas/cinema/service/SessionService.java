package ua.lviv.bas.cinema.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.dto.cinemaHall.SessionDto;
import ua.lviv.bas.cinema.dto.cinemaHall.SessionRequest;
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

	@Transactional(readOnly = true)
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
	public Page<SessionDto> getAllSessions(Pageable pageable, String search) {
		log.debug("Retrieving all sessions with pagination");
		Page<Session> sessions;

		if (search != null && !search.trim().isEmpty()) {
			sessions = sessionRepository.findByMovieTitleContainingIgnoreCase(search, pageable);
		} else {
			sessions = sessionRepository.findAll(pageable);
		}

		return sessions.map(sessionMapper::toDto);
	}

	@Transactional(readOnly = true)
	public Page<SessionDto> getSessionsByDate(LocalDate date, Pageable pageable) {
		log.debug("Retrieving sessions for date: {} with pagination", date);
		LocalDateTime startOfDay = date.atStartOfDay();
		LocalDateTime endOfDay = date.atTime(23, 59, 59);

		Page<Session> sessions = sessionRepository.findByStartTimeBetween(startOfDay, endOfDay, pageable);
		return sessions.map(sessionMapper::toDto);
	}

	@Transactional(readOnly = true)
	public Page<SessionDto> getSessionsByHall(Long hallId, Pageable pageable) {
		log.debug("Retrieving sessions for hall: {} with pagination", hallId);
		Page<Session> sessions = sessionRepository.findByHallId(hallId, pageable);
		return sessions.map(sessionMapper::toDto);
	}

	@Transactional(readOnly = true)
	public Page<SessionDto> getSessionsByMovie(Long movieId, Pageable pageable) {
		log.debug("Retrieving sessions for movie: {} with pagination", movieId);
		Page<Session> sessions = sessionRepository.findByMovieId(movieId, pageable);
		return sessions.map(sessionMapper::toDto);
	}

	@Transactional(readOnly = true)
	public Page<SessionDto> getAvailableSessions(Pageable pageable) {
		log.debug("Retrieving available sessions with pagination");
		Page<Session> sessions = sessionRepository.findByStartTimeAfter(LocalDateTime.now(), pageable);
		return sessions.map(sessionMapper::toDto);
	}

	@Transactional(readOnly = true)
	public Page<SessionDto> getUpcomingSessions(int days, Pageable pageable) {
		log.debug("Retrieving upcoming sessions for next {} days with pagination", days);
		LocalDateTime start = LocalDateTime.now();
		LocalDateTime end = start.plusDays(days);

		Page<Session> sessions = sessionRepository.findByStartTimeBetween(start, end, pageable);
		return sessions.map(sessionMapper::toDto);
	}

	@Transactional(readOnly = true)
	public Page<SessionDto> getTodaySessions(Pageable pageable) {
		log.debug("Retrieving today's sessions with pagination");
		return getSessionsByDate(LocalDate.now(), pageable);
	}

	@Transactional(readOnly = true)
	public boolean hasTimeConflict(Long hallId, LocalDateTime startTime, Integer durationMinutes,
			Long excludeSessionId) {
		LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

		List<Session> conflictingSessions = sessionRepository.findConflictingSessions(hallId, startTime, endTime,
				excludeSessionId);

		return !conflictingSessions.isEmpty();
	}
}