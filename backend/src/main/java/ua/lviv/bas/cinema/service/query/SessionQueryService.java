package ua.lviv.bas.cinema.service.query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.querydsl.core.BooleanBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.QSession;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.filter.SessionFilter;
import ua.lviv.bas.cinema.repository.SessionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionQueryService {

	private final SessionRepository sessionRepository;
	private final QSession qSession = QSession.session;

	public List<Session> findSessionsToStart() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime fiveMinutesAgo = now.minusMinutes(5);

		BooleanBuilder predicate = new BooleanBuilder();
		predicate.and(qSession.status.eq(CinemaSessionStatus.SCHEDULED));
		predicate.and(qSession.startTime.loe(now));
		predicate.and(qSession.startTime.goe(fiveMinutesAgo));

		return (List<Session>) sessionRepository.findAll(predicate);
	}

	public List<Session> findSessionsToComplete() {
		LocalDateTime now = LocalDateTime.now();

		BooleanBuilder predicate = new BooleanBuilder();
		predicate.and(qSession.status.eq(CinemaSessionStatus.ONGOING));

		List<Session> ongoingSessions = (List<Session>) sessionRepository.findAll(predicate);

		return ongoingSessions.stream().filter(session -> {
			LocalDateTime sessionEnd = calculateEndTime(session);
			return sessionEnd != null && sessionEnd.isBefore(now);
		}).collect(Collectors.toList());
	}

	public Page<Session> findFilteredSessions(SessionFilter filter) {
		validateFilter(filter);
		BooleanBuilder predicate = new BooleanBuilder();

		if (filter.getStartTime() != null) {
			predicate.and(qSession.startTime.goe(filter.getStartTime()));
		}
		if (filter.getEndTime() != null) {
			predicate.and(qSession.startTime.loe(filter.getEndTime()));
		}
		if (filter.getHallId() != null) {
			predicate.and(qSession.hall.id.eq(filter.getHallId()));
		}
		if (filter.getMovieId() != null) {
			predicate.and(qSession.movie.id.eq(filter.getMovieId()));
		}
		if (filter.getStatus() != null) {
			predicate.and(qSession.status.eq(filter.getStatus()));
		}
		if (!filter.isAdminView()) {
			predicate.and(qSession.startTime.after(LocalDateTime.now()));
			predicate.and(qSession.status.eq(CinemaSessionStatus.SCHEDULED));
		}

		Sort sort = Sort.by(filter.getSortDirection() == SessionFilter.SortDirection.DESC ? Sort.Direction.DESC
				: Sort.Direction.ASC, filter.getSortBy());
		Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
		return sessionRepository.findAll(predicate, pageable);
	}

	public List<Session> findConflictingSessions(Long hallId, LocalDateTime startTime, LocalDateTime endTime,
			Long excludeSessionId) {
		BooleanBuilder predicate = new BooleanBuilder();
		predicate.and(qSession.hall.id.eq(hallId));
		predicate.and(qSession.startTime.lt(endTime));
		predicate.and(qSession.status.in(CinemaSessionStatus.SCHEDULED, CinemaSessionStatus.ONGOING));
		if (excludeSessionId != null) {
			predicate.and(qSession.id.ne(excludeSessionId));
		}
		List<Session> allSessions = (List<Session>) sessionRepository.findAll(predicate);
		return allSessions.stream().filter(session -> {
			LocalDateTime sessionEnd = calculateEndTime(session);
			return sessionEnd != null && sessionEnd.isAfter(startTime);
		}).collect(Collectors.toList());
	}

	public Page<Session> findByMovieTitle(String search, Pageable pageable, boolean adminView) {
		BooleanBuilder predicate = new BooleanBuilder();
		if (StringUtils.hasText(search)) {
			String searchTerm = "%" + search.toLowerCase() + "%";
			predicate.and(qSession.movie.title.lower().like(searchTerm));
		}
		addPublicViewFilters(predicate, adminView);
		return sessionRepository.findAll(predicate, pageable);
	}

	public Page<Session> findByStartTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable,
			boolean adminView) {
		BooleanBuilder predicate = new BooleanBuilder();
		predicate.and(qSession.startTime.between(start, end));
		addPublicViewFilters(predicate, adminView);
		return sessionRepository.findAll(predicate, pageable);
	}

	public Page<Session> findByHallId(Long hallId, Pageable pageable, boolean adminView) {
		BooleanBuilder predicate = new BooleanBuilder();
		predicate.and(qSession.hall.id.eq(hallId));
		addPublicViewFilters(predicate, adminView);
		return sessionRepository.findAll(predicate, pageable);
	}

	public Page<Session> findByMovieId(Long movieId, Pageable pageable, boolean adminView) {
		BooleanBuilder predicate = new BooleanBuilder();
		predicate.and(qSession.movie.id.eq(movieId));
		addPublicViewFilters(predicate, adminView);
		return sessionRepository.findAll(predicate, pageable);
	}

	public Page<Session> findAvailableSessions(Pageable pageable) {
		BooleanBuilder predicate = new BooleanBuilder();
		predicate.and(qSession.startTime.after(LocalDateTime.now()));
		predicate.and(qSession.status.eq(CinemaSessionStatus.SCHEDULED));
		return sessionRepository.findAll(predicate, pageable);
	}

	public Page<Session> findByStatus(CinemaSessionStatus status, Pageable pageable) {
		BooleanBuilder predicate = new BooleanBuilder();
		predicate.and(qSession.status.eq(status));
		if (status == CinemaSessionStatus.SCHEDULED) {
			predicate.and(qSession.startTime.after(LocalDateTime.now()));
		}
		return sessionRepository.findAll(predicate, pageable);
	}

	private void addPublicViewFilters(BooleanBuilder predicate, boolean adminView) {
		if (!adminView) {
			predicate.and(qSession.startTime.after(LocalDateTime.now()));
			predicate.and(qSession.status.eq(CinemaSessionStatus.SCHEDULED));
		}
	}

	private void validateFilter(SessionFilter filter) {
		if (filter.getStartTime() != null && filter.getEndTime() != null
				&& filter.getStartTime().isAfter(filter.getEndTime())) {
			throw new IllegalArgumentException("startTime cannot be after endTime");
		}
		if (!filter.isAdminView() && filter.getStatus() != null
				&& filter.getStatus() != CinemaSessionStatus.SCHEDULED) {
			throw new IllegalArgumentException("Users can only view SCHEDULED sessions");
		}
		if (filter.getSize() > 100) {
			throw new IllegalArgumentException("Page size cannot exceed 100");
		}
	}

	private LocalDateTime calculateEndTime(Session session) {
		if (session == null || session.getMovie() == null || session.getMovie().getDurationMinutes() == null
				|| session.getStartTime() == null) {
			return null;
		}
		return session.getStartTime().plusMinutes(session.getMovie().getDurationMinutes());
	}
}