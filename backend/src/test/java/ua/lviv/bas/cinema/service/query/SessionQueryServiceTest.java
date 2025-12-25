package ua.lviv.bas.cinema.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
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
		session.setStatus(CinemaSessionStatus.SCHEDULED);

		CinemaHall hall = new CinemaHall();
		hall.setId(1L);
		session.setHall(hall);

		Movie movie = new Movie();
		movie.setId(1L);
		movie.setTitle("Test Movie");
		movie.setDurationMinutes(90);
		session.setMovie(movie);

		pageable = PageRequest.of(0, 10);
	}

	@Test
	void findFilteredSessions_ShouldReturnFilteredSessions() {
		SessionFilter filter = SessionFilter.builder().page(0).size(10).sortBy("startTime")
				.sortDirection(SessionFilter.SortDirection.ASC).startTime(now).endTime(now.plusDays(1)).adminView(true)
				.build();

		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findFilteredSessions(filter);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
	}

	@Test
	void findFilteredSessions_ShouldFilterByStatus() {
		SessionFilter filter = SessionFilter.builder().status(CinemaSessionStatus.SCHEDULED).adminView(true).build();

		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findFilteredSessions(filter);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findFilteredSessions_ShouldThrowException_WhenUserViewsNonScheduled() {
		SessionFilter filter = SessionFilter.builder().status(CinemaSessionStatus.CANCELLED).adminView(false).build();

		assertThatThrownBy(() -> sessionQueryService.findFilteredSessions(filter))
				.isInstanceOf(IllegalArgumentException.class).hasMessage("Users can only view SCHEDULED sessions");
	}

	@Test
	void findFilteredSessions_ShouldThrowException_WhenInvalidDateRange() {
		SessionFilter filter = SessionFilter.builder().startTime(now.plusDays(1)).endTime(now).adminView(true).build();

		assertThatThrownBy(() -> sessionQueryService.findFilteredSessions(filter))
				.isInstanceOf(IllegalArgumentException.class).hasMessage("startTime cannot be after endTime");
	}

	@Test
	void findFilteredSessions_ShouldThrowException_WhenPageSizeTooLarge() {
		SessionFilter filter = SessionFilter.builder().size(150).adminView(true).build();

		assertThatThrownBy(() -> sessionQueryService.findFilteredSessions(filter))
				.isInstanceOf(IllegalArgumentException.class).hasMessage("Page size cannot exceed 100");
	}

	@Test
	void findFilteredSessions_ShouldApplyDefaultSorting() {
		SessionFilter filter = SessionFilter.builder().adminView(true).build();

		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findFilteredSessions(filter);

		assertThat(result).isNotNull();
	}

	@Test
	void findConflictingSessions_ShouldReturnConflictingSessions() {
		LocalDateTime startTime = now;
		LocalDateTime endTime = now.plusHours(2);

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

		when(sessionRepository.findAll(any(Predicate.class))).thenReturn(List.of(conflictingSession));

		List<Session> result = sessionQueryService.findConflictingSessions(1L, startTime, endTime, null);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getId()).isEqualTo(2L);
	}

	@Test
	void findConflictingSessions_ShouldReturnEmpty_WhenNoConflict() {
		LocalDateTime startTime = now;
		LocalDateTime endTime = now.plusHours(2);

		Session nonConflictingSession = new Session();
		nonConflictingSession.setId(2L);
		nonConflictingSession.setStartTime(startTime.minusHours(2));

		CinemaHall hall = new CinemaHall();
		hall.setId(1L);
		nonConflictingSession.setHall(hall);

		Movie movie = new Movie();
		movie.setId(1L);
		movie.setDurationMinutes(90);
		nonConflictingSession.setMovie(movie);

		when(sessionRepository.findAll(any(Predicate.class))).thenReturn(List.of(nonConflictingSession));

		List<Session> result = sessionQueryService.findConflictingSessions(1L, startTime, endTime, null);

		assertThat(result).isEmpty();
	}

	@Test
	void findConflictingSessions_ShouldExcludeSession() {
		LocalDateTime startTime = now;
		LocalDateTime endTime = now.plusHours(2);

		when(sessionRepository.findAll(any(Predicate.class))).thenReturn(List.of());

		List<Session> result = sessionQueryService.findConflictingSessions(1L, startTime, endTime, 2L);

		assertThat(result).isEmpty();
	}

	@Test
	void findByMovieTitle_ShouldReturnSessions() {
		String search = "test";
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findByMovieTitle(search, pageable, false);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findByMovieTitle_ShouldReturnAllSessions_WhenSearchIsEmpty() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findByMovieTitle(null, pageable, false);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findByMovieTitle_ShouldReturnAllSessions_WhenSearchIsBlank() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findByMovieTitle("   ", pageable, false);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findByMovieTitle_AdminView_ShouldReturnAllStatuses() {
		session.setStatus(CinemaSessionStatus.CANCELLED);
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findByMovieTitle("test", pageable, true);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findByStartTimeBetween_ShouldReturnSessions() {
		LocalDateTime start = now;
		LocalDateTime end = now.plusDays(1);

		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findByStartTimeBetween(start, end, pageable, false);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findByStartTimeBetween_AdminView_ShouldReturnPastSessions() {
		LocalDateTime start = now.minusDays(1);
		LocalDateTime end = now.plusDays(1);

		Session pastSession = new Session();
		pastSession.setId(2L);
		pastSession.setStartTime(now.minusHours(1));
		pastSession.setStatus(CinemaSessionStatus.COMPLETED);

		Page<Session> sessionPage = new PageImpl<>(List.of(pastSession));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findByStartTimeBetween(start, end, pageable, true);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getStatus()).isEqualTo(CinemaSessionStatus.COMPLETED);
	}

	@Test
	void findByHallId_ShouldReturnSessions() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findByHallId(1L, pageable, false);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findByHallId_ShouldReturnEmpty_WhenNoSessions() {
		Page<Session> sessionPage = new PageImpl<>(List.of());

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findByHallId(999L, pageable, false);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).isEmpty();
	}

	@Test
	void findByMovieId_ShouldReturnSessions() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findByMovieId(1L, pageable, false);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findAvailableSessions_ShouldReturnOnlyScheduledSessions() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findAvailableSessions(pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
	}

	@Test
	void findAvailableSessions_ShouldExcludePastSessions() {
		Session pastSession = new Session();
		pastSession.setId(2L);
		pastSession.setStartTime(now.minusHours(1));
		pastSession.setStatus(CinemaSessionStatus.SCHEDULED);

		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findAvailableSessions(pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
	}

	@Test
	void findFiltered_ShouldReturnSessions() {
		LocalDateTime startTime = now;
		LocalDateTime endTime = now.plusDays(1);

		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findFiltered(startTime, endTime, 1L, 1L,
				CinemaSessionStatus.SCHEDULED, pageable, false);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findFiltered_ShouldReturnSessions_WhenOnlyHallIdProvided() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findFiltered(null, null, 1L, null, CinemaSessionStatus.SCHEDULED,
				pageable, false);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findFiltered_ShouldReturnSessions_WhenOnlyMovieIdProvided() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findFiltered(null, null, null, 1L, CinemaSessionStatus.SCHEDULED,
				pageable, false);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findFiltered_ShouldReturnSessions_WhenOnlyDateRangeProvided() {
		LocalDateTime startTime = now;
		LocalDateTime endTime = now.plusDays(1);

		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findFiltered(startTime, endTime, null, null,
				CinemaSessionStatus.SCHEDULED, pageable, false);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findFiltered_ShouldReturnAllAvailableSessions_WhenNoFilters() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findFiltered(null, null, null, null, CinemaSessionStatus.SCHEDULED,
				pageable, false);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findFiltered_AdminView_ShouldReturnAllStatuses() {
		session.setStatus(CinemaSessionStatus.CANCELLED);
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findFiltered(null, null, null, null, CinemaSessionStatus.CANCELLED,
				pageable, true);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findByStatus_ShouldReturnSessionsWithStatus() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findByStatus(CinemaSessionStatus.SCHEDULED, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
	}

	@Test
	void findByStatus_ShouldFilterFutureSessions_ForScheduledStatus() {
		Session pastSession = new Session();
		pastSession.setId(2L);
		pastSession.setStartTime(now.minusHours(1));
		pastSession.setStatus(CinemaSessionStatus.SCHEDULED);

		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findByStatus(CinemaSessionStatus.SCHEDULED, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
	}

	@Test
	void findFilteredSessions_ShouldApplyUserRestrictions() {
		SessionFilter filter = SessionFilter.builder().adminView(false).build();

		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(sessionPage);

		Page<Session> result = sessionQueryService.findFilteredSessions(filter);

		assertThat(result).isNotNull();
	}
}