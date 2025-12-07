package ua.lviv.bas.cinema.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.dto.filter.SessionFilter;
import ua.lviv.bas.cinema.repository.SessionRepository;

@ExtendWith(MockitoExtension.class)
class SessionQueryServiceTest {

	@Mock
	private SessionRepository sessionRepository;

	@InjectMocks
	private SessionQueryService sessionQueryService;

	private Session session;
	private Pageable pageable;
	private LocalDateTime now;

	@BeforeEach
	void setUp() {
		now = LocalDateTime.now();

		session = new Session();
		session.setId(1L);
		session.setStartTime(now.plusHours(2));

		pageable = PageRequest.of(0, 10);
	}

	@Test
	void findFilteredSessions_ShouldReturnFilteredSessions() {
		SessionFilter filter = new SessionFilter();
		filter.setPage(0);
		filter.setSize(10);
		filter.setSortBy("startTime");
		filter.setSortDirection(SessionFilter.SortDirection.ASC);
		filter.setStartTime(now);
		filter.setEndTime(now.plusDays(1));

		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findFilteredSessions(filter);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
	}

	@Test
	void findConflictingSessions_ShouldReturnConflictingSessions() {
		LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 18, 0);
		LocalDateTime endTime = startTime.plusHours(2);

		Session conflictingSession = new Session();
		conflictingSession.setId(2L);
		conflictingSession.setStartTime(startTime.plusMinutes(30));

		CinemaHall hall = new CinemaHall();
		hall.setId(1L);
		conflictingSession.setHall(hall);

		Movie movie = new Movie();
		movie.setId(1L);
		movie.setDurationMinutes(90);
		conflictingSession.setMovie(movie);

		List<Session> allSessions = List.of(conflictingSession);

		when(sessionRepository.findAll(any(Predicate.class))).thenReturn(allSessions);

		List<Session> result = sessionQueryService.findConflictingSessions(1L, startTime, endTime, null);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getId()).isEqualTo(2L);
	}

	@Test
	void findConflictingSessions_ShouldExcludeSession() {
		LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 18, 0);
		LocalDateTime endTime = startTime.plusHours(2);

		when(sessionRepository.findAll(any(Predicate.class))).thenReturn(List.of());

		List<Session> result = sessionQueryService.findConflictingSessions(1L, startTime, endTime, 2L);

		assertThat(result).isEmpty();
	}

	@Test
	void findByMovieTitle_ShouldReturnSessions() {
		String search = "test";
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findByMovieTitle(search, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findByMovieTitle_ShouldReturnAllSessions_WhenSearchIsEmpty() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findByMovieTitle(null, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findByStartTimeBetween_ShouldReturnSessions() {
		LocalDateTime start = now;
		LocalDateTime end = now.plusDays(1);

		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findByStartTimeBetween(start, end, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findByHallId_ShouldReturnSessions() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findByHallId(1L, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findByMovieId_ShouldReturnSessions() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findByMovieId(1L, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findAvailableSessions_ShouldReturnSessions() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findAvailableSessions(pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findFiltered_ShouldReturnSessions() {
		LocalDateTime startTime = now;
		LocalDateTime endTime = now.plusDays(1);

		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findFiltered(startTime, endTime, 1L, 1L, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findFiltered_ShouldReturnSessions_WhenOnlyHallIdProvided() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findFiltered(null, null, 1L, null, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findFiltered_ShouldReturnSessions_WhenOnlyMovieIdProvided() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findFiltered(null, null, null, 1L, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findFiltered_ShouldReturnSessions_WhenOnlyDateRangeProvided() {
		LocalDateTime startTime = now;
		LocalDateTime endTime = now.plusDays(1);

		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findFiltered(startTime, endTime, null, null, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findFiltered_ShouldReturnAllAvailableSessions_WhenNoFilters() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findFiltered(null, null, null, null, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}
}