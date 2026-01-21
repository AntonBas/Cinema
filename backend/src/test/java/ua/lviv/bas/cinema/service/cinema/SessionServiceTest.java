package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionUpdateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionOperationException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionTimeConflictException;
import ua.lviv.bas.cinema.mapper.SessionMapper;
import ua.lviv.bas.cinema.repository.MovieRepository;
import ua.lviv.bas.cinema.repository.SessionRepository;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {

	@Mock
	private SessionRepository sessionRepository;

	@Mock
	private SessionMapper sessionMapper;

	@Mock
	private MovieRepository movieRepository;

	@Mock
	private CinemaHallService cinemaHallService;

	@InjectMocks
	private SessionService sessionService;

	private Movie testMovie;
	private CinemaHall testHall;
	private Session testSession;
	private LocalDateTime testStartTime;

	@BeforeEach
	void setUp() {
		testStartTime = LocalDateTime.now().plusHours(2);

		testMovie = new Movie();
		testMovie.setId(1L);
		testMovie.setTitle("Test Movie");
		testMovie.setDurationMinutes(120);
		testMovie.setReleaseDate(LocalDate.now().minusDays(1));
		testMovie.setEndShowingDate(LocalDate.now().plusDays(30));

		testHall = new CinemaHall();
		testHall.setId(1L);
		testHall.setName("Test Hall");

		testSession = new Session();
		testSession.setId(1L);
		testSession.setStartTime(testStartTime);
		testSession.setBasePrice(BigDecimal.valueOf(250));
		testSession.setMovie(testMovie);
		testSession.setHall(testHall);
		testSession.setStatus(CinemaSessionStatus.SCHEDULED);
	}

	@Test
	void createSession_Success() {
		SessionCreateRequest request = SessionCreateRequest.builder().startTime(testStartTime)
				.basePrice(BigDecimal.valueOf(250)).movieId(1L).hallId(1L).build();

		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();

		when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
		when(cinemaHallService.getHallEntityById(1L)).thenReturn(testHall);
		when(sessionRepository.existsConflictingSession(anyLong(), any(), any(), any())).thenReturn(false);
		when(sessionMapper.toSession(request)).thenReturn(testSession);
		when(sessionRepository.save(testSession)).thenReturn(testSession);
		when(sessionMapper.toSessionAdminResponse(testSession)).thenReturn(response);

		SessionAdminResponse result = sessionService.createSession(request);

		assertThat(result).isNotNull();
		verify(sessionRepository).save(testSession);
		verify(sessionRepository).existsConflictingSession(anyLong(), any(), any(), any());
	}

	@Test
	void createSession_WhenMovieNotFound_ShouldThrowException() {
		SessionCreateRequest request = SessionCreateRequest.builder().startTime(testStartTime).movieId(1L).hallId(1L)
				.build();

		when(movieRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> sessionService.createSession(request)).isInstanceOf(MovieNotFoundException.class);
	}

	@Test
	void createSession_WhenTimeConflict_ShouldThrowException() {
		SessionCreateRequest request = SessionCreateRequest.builder().startTime(testStartTime).movieId(1L).hallId(1L)
				.build();

		when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
		when(cinemaHallService.getHallEntityById(1L)).thenReturn(testHall);
		when(sessionRepository.existsConflictingSession(anyLong(), any(), any(), any())).thenReturn(true);

		assertThatThrownBy(() -> sessionService.createSession(request))
				.isInstanceOf(SessionTimeConflictException.class);
	}

	@Test
	void getSessionById_Success() {
		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
		when(sessionMapper.toSessionAdminResponse(testSession)).thenReturn(response);

		SessionAdminResponse result = sessionService.getSessionById(1L);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
	}

	@Test
	void getSessionById_WhenNotFound_ShouldThrowException() {
		when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> sessionService.getSessionById(1L)).isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void getSessionByIdForPublic_Success() {
		SessionScheduleResponse response = SessionScheduleResponse.builder().id(1L).build();

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
		when(sessionMapper.toSessionScheduleResponse(testSession)).thenReturn(response);

		SessionScheduleResponse result = sessionService.getSessionByIdForPublic(1L);

		assertThat(result).isNotNull();
	}

	@Test
	void getSessionByIdForPublic_WhenInactiveStatus_ShouldThrowException() {
		testSession.setStatus(CinemaSessionStatus.CANCELLED);

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));

		assertThatThrownBy(() -> sessionService.getSessionByIdForPublic(1L))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void getSessionByIdForPublic_WhenPastSession_ShouldThrowException() {
		testSession.setStartTime(LocalDateTime.now().minusHours(1));

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));

		assertThatThrownBy(() -> sessionService.getSessionByIdForPublic(1L))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void updateSession_Success() {
		SessionUpdateRequest request = SessionUpdateRequest.builder().basePrice(BigDecimal.valueOf(300)).build();

		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();

		when(sessionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testSession));
		doNothing().when(sessionMapper).updateSessionFromRequest(request, testSession);
		when(sessionRepository.save(testSession)).thenReturn(testSession);
		when(sessionMapper.toSessionAdminResponse(testSession)).thenReturn(response);

		SessionAdminResponse result = sessionService.updateSession(1L, request);

		assertThat(result).isNotNull();
		verify(sessionRepository).findByIdWithLock(1L);
	}

	@Test
	void updateSession_WhenTimeConflict_ShouldThrowException() {
		LocalDateTime newStartTime = testStartTime.plusHours(3);
		SessionUpdateRequest request = SessionUpdateRequest.builder().startTime(newStartTime).build();

		when(sessionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testSession));
		when(sessionRepository.existsConflictingSession(anyLong(), any(), any(), any())).thenReturn(true);

		assertThatThrownBy(() -> sessionService.updateSession(1L, request))
				.isInstanceOf(SessionTimeConflictException.class);
	}

	@Test
	void deleteSession_Success() {
		when(sessionRepository.existsById(1L)).thenReturn(true);
		doNothing().when(sessionRepository).deleteById(1L);

		sessionService.deleteSession(1L);

		verify(sessionRepository).deleteById(1L);
	}

	@Test
	void deleteSession_WhenNotFound_ShouldThrowException() {
		when(sessionRepository.existsById(1L)).thenReturn(false);

		assertThatThrownBy(() -> sessionService.deleteSession(1L)).isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void cancelSession_Success() {
		when(sessionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testSession));
		when(sessionRepository.save(testSession)).thenReturn(testSession);

		sessionService.cancelSession(1L);

		assertThat(testSession.getStatus()).isEqualTo(CinemaSessionStatus.CANCELLED);
		verify(sessionRepository).save(testSession);
		verify(sessionRepository).findByIdWithLock(1L);
	}

	@Test
	void cancelSession_WhenAlreadyCancelled_ShouldDoNothing() {
		testSession.setStatus(CinemaSessionStatus.CANCELLED);

		when(sessionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testSession));

		sessionService.cancelSession(1L);

		verify(sessionRepository, never()).save(any());
	}

	@Test
	void cancelSession_WhenInactiveStatus_ShouldThrowException() {
		testSession.setStatus(CinemaSessionStatus.COMPLETED);

		when(sessionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testSession));

		assertThatThrownBy(() -> sessionService.cancelSession(1L)).isInstanceOf(SessionOperationException.class)
				.hasMessageContaining("Cannot cancel inactive session");
	}

	@Test
	void reactivateSession_Success() {
		testSession.setStatus(CinemaSessionStatus.CANCELLED);

		when(sessionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testSession));
		when(sessionRepository.existsConflictingSession(anyLong(), any(), any(), any())).thenReturn(false);
		when(sessionRepository.save(testSession)).thenReturn(testSession);

		sessionService.reactivateSession(1L);

		assertThat(testSession.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
		verify(sessionRepository).save(testSession);
		verify(sessionRepository).findByIdWithLock(1L);
	}

	@Test
	void reactivateSession_WhenTimeConflict_ShouldThrowException() {
		testSession.setStatus(CinemaSessionStatus.CANCELLED);

		when(sessionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testSession));
		when(sessionRepository.existsConflictingSession(anyLong(), any(), any(), any())).thenReturn(true);

		assertThatThrownBy(() -> sessionService.reactivateSession(1L)).isInstanceOf(SessionTimeConflictException.class);
	}

	@Test
	void getSessionsForAdmin_WithSearch() {
		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Session> page = new PageImpl<>(List.of(testSession));

		when(sessionRepository.findByMovieTitleWithMovieAndHall(eq("test"), eq(true), eq(CinemaSessionStatus.SCHEDULED),
				eq(pageable))).thenReturn(page);
		when(sessionMapper.toSessionAdminResponse(testSession)).thenReturn(response);

		Page<SessionAdminResponse> result = sessionService.getSessionsForAdmin("test", null, null, null, null,
				pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findByMovieTitleWithMovieAndHall(eq("test"), eq(true),
				eq(CinemaSessionStatus.SCHEDULED), eq(pageable));
	}

	@Test
	void getSessionsForAdmin_WithDateOnly() {
		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Session> page = new PageImpl<>(List.of(testSession));

		when(sessionRepository.findByStartTimeBetweenWithMovieAndHall(any(LocalDateTime.class),
				any(LocalDateTime.class), eq(true), eq(CinemaSessionStatus.SCHEDULED), eq(pageable))).thenReturn(page);
		when(sessionMapper.toSessionAdminResponse(testSession)).thenReturn(response);

		Page<SessionAdminResponse> result = sessionService.getSessionsForAdmin(null, LocalDate.now(), null, null, null,
				pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findByStartTimeBetweenWithMovieAndHall(any(LocalDateTime.class),
				any(LocalDateTime.class), eq(true), eq(CinemaSessionStatus.SCHEDULED), eq(pageable));
	}

	@Test
	void getSessionsForAdmin_WithHallIdOnly() {
		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Session> page = new PageImpl<>(List.of(testSession));

		when(sessionRepository.findByHallIdWithMovieAndHall(eq(1L), eq(true), eq(CinemaSessionStatus.SCHEDULED),
				eq(pageable))).thenReturn(page);
		when(sessionMapper.toSessionAdminResponse(testSession)).thenReturn(response);

		Page<SessionAdminResponse> result = sessionService.getSessionsForAdmin(null, null, 1L, null, null, pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findByHallIdWithMovieAndHall(eq(1L), eq(true), eq(CinemaSessionStatus.SCHEDULED),
				eq(pageable));
	}

	@Test
	void getSessionsForAdmin_WithMovieIdOnly() {
		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Session> page = new PageImpl<>(List.of(testSession));

		when(sessionRepository.findByMovieIdWithMovieAndHall(eq(1L), eq(true), eq(CinemaSessionStatus.SCHEDULED),
				eq(pageable))).thenReturn(page);
		when(sessionMapper.toSessionAdminResponse(testSession)).thenReturn(response);

		Page<SessionAdminResponse> result = sessionService.getSessionsForAdmin(null, null, null, 1L, null, pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findByMovieIdWithMovieAndHall(eq(1L), eq(true), eq(CinemaSessionStatus.SCHEDULED),
				eq(pageable));
	}

	@Test
	void getSessionsForAdmin_WithStatusOnly() {
		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Session> page = new PageImpl<>(List.of(testSession));

		when(sessionRepository.findByStatusWithMovieAndHall(eq(CinemaSessionStatus.SCHEDULED), eq(pageable)))
				.thenReturn(page);
		when(sessionMapper.toSessionAdminResponse(testSession)).thenReturn(response);

		Page<SessionAdminResponse> result = sessionService.getSessionsForAdmin(null, null, null, null,
				CinemaSessionStatus.SCHEDULED, pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findByStatusWithMovieAndHall(eq(CinemaSessionStatus.SCHEDULED), eq(pageable));
	}

	@Test
	void getSessionsForAdmin_NoFilters() {
		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Session> page = new PageImpl<>(List.of(testSession));

		when(sessionRepository.findAllWithMovieAndHall(eq(pageable))).thenReturn(page);
		when(sessionMapper.toSessionAdminResponse(testSession)).thenReturn(response);

		Page<SessionAdminResponse> result = sessionService.getSessionsForAdmin(null, null, null, null, null, pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findAllWithMovieAndHall(eq(pageable));
	}

	@Test
	void getSessionsForAdmin_WithSearchAndAdditionalFilters() {
		Session session1 = new Session();
		session1.setId(1L);
		session1.setMovie(testMovie);
		session1.setHall(testHall);
		session1.setStatus(CinemaSessionStatus.SCHEDULED);
		session1.setStartTime(testStartTime);

		Session session2 = new Session();
		session2.setId(2L);
		session2.setMovie(testMovie);
		CinemaHall otherHall = new CinemaHall();
		otherHall.setId(2L);
		session2.setHall(otherHall);
		session2.setStatus(CinemaSessionStatus.SCHEDULED);
		session2.setStartTime(testStartTime.plusHours(1));

		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Session> page = new PageImpl<>(List.of(session1, session2));

		when(sessionRepository.findByMovieTitleWithMovieAndHall(eq("test"), eq(true), eq(CinemaSessionStatus.SCHEDULED),
				eq(pageable))).thenReturn(page);
		when(sessionMapper.toSessionAdminResponse(session1)).thenReturn(response);

		Page<SessionAdminResponse> result = sessionService.getSessionsForAdmin("test", null, 1L, null, null, pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findByMovieTitleWithMovieAndHall(eq("test"), eq(true),
				eq(CinemaSessionStatus.SCHEDULED), eq(pageable));
	}

	@Test
	void getSessionsForAdmin_WithDateAndOtherFilters() {
		LocalDate testDate = LocalDate.now();
		Session session1 = new Session();
		session1.setId(1L);
		session1.setMovie(testMovie);
		session1.setHall(testHall);
		session1.setStatus(CinemaSessionStatus.SCHEDULED);
		session1.setStartTime(testDate.atTime(14, 0));

		Session session2 = new Session();
		session2.setId(2L);
		session2.setMovie(testMovie);
		session2.setHall(testHall);
		session2.setStatus(CinemaSessionStatus.SCHEDULED);
		session2.setStartTime(testDate.plusDays(1).atTime(14, 0));

		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Session> page = new PageImpl<>(List.of(session1, session2));

		when(sessionRepository.findByMovieTitleWithMovieAndHall(eq("test"), eq(true), eq(CinemaSessionStatus.SCHEDULED),
				eq(pageable))).thenReturn(page);
		when(sessionMapper.toSessionAdminResponse(session1)).thenReturn(response);

		Page<SessionAdminResponse> result = sessionService.getSessionsForAdmin("test", testDate, null, null, null,
				pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findByMovieTitleWithMovieAndHall(eq("test"), eq(true),
				eq(CinemaSessionStatus.SCHEDULED), eq(pageable));
	}

	@Test
	void getTodaySessions() {
		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Session> page = new PageImpl<>(List.of(testSession));

		when(sessionRepository.findByStartTimeBetweenWithMovieAndHall(any(LocalDateTime.class),
				any(LocalDateTime.class), eq(true), eq(CinemaSessionStatus.SCHEDULED), eq(pageable))).thenReturn(page);
		when(sessionMapper.toSessionAdminResponse(testSession)).thenReturn(response);

		Page<SessionAdminResponse> result = sessionService.getTodaySessions(pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findByStartTimeBetweenWithMovieAndHall(any(LocalDateTime.class),
				any(LocalDateTime.class), eq(true), eq(CinemaSessionStatus.SCHEDULED), eq(pageable));
	}

	@Test
	void getScheduleSessions_WithDate() {
		SessionScheduleResponse response = SessionScheduleResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Session> page = new PageImpl<>(List.of(testSession));

		when(sessionRepository.findByStartTimeBetweenWithMovieAndHall(any(LocalDateTime.class),
				any(LocalDateTime.class), eq(false), eq(CinemaSessionStatus.SCHEDULED), eq(pageable))).thenReturn(page);
		when(sessionMapper.toSessionScheduleResponse(testSession)).thenReturn(response);

		Page<SessionScheduleResponse> result = sessionService.getScheduleSessions(LocalDate.now(), null, null,
				pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findByStartTimeBetweenWithMovieAndHall(any(LocalDateTime.class),
				any(LocalDateTime.class), eq(false), eq(CinemaSessionStatus.SCHEDULED), eq(pageable));
	}

	@Test
	void getScheduleSessions_WithMovieId() {
		SessionScheduleResponse response = SessionScheduleResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Session> page = new PageImpl<>(List.of(testSession));

		when(sessionRepository.findByMovieIdWithMovieAndHall(eq(1L), eq(false), eq(CinemaSessionStatus.SCHEDULED),
				eq(pageable))).thenReturn(page);
		when(sessionMapper.toSessionScheduleResponse(testSession)).thenReturn(response);

		Page<SessionScheduleResponse> result = sessionService.getScheduleSessions(null, 1L, null, pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findByMovieIdWithMovieAndHall(eq(1L), eq(false), eq(CinemaSessionStatus.SCHEDULED),
				eq(pageable));
	}

	@Test
	void getScheduleSessions_WithDaysAhead() {
		SessionScheduleResponse response = SessionScheduleResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Session> page = new PageImpl<>(List.of(testSession));

		when(sessionRepository.findByStartTimeBetweenWithMovieAndHall(any(LocalDateTime.class),
				any(LocalDateTime.class), eq(false), eq(CinemaSessionStatus.SCHEDULED), eq(pageable))).thenReturn(page);
		when(sessionMapper.toSessionScheduleResponse(testSession)).thenReturn(response);

		Page<SessionScheduleResponse> result = sessionService.getScheduleSessions(null, null, 7, pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findByStartTimeBetweenWithMovieAndHall(any(LocalDateTime.class),
				any(LocalDateTime.class), eq(false), eq(CinemaSessionStatus.SCHEDULED), eq(pageable));
	}

	@Test
	void getScheduleSessions_NoFilters() {
		SessionScheduleResponse response = SessionScheduleResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Session> page = new PageImpl<>(List.of(testSession));

		when(sessionRepository.findAvailableSessionsWithMovieAndHall(eq(CinemaSessionStatus.SCHEDULED), eq(pageable)))
				.thenReturn(page);
		when(sessionMapper.toSessionScheduleResponse(testSession)).thenReturn(response);

		Page<SessionScheduleResponse> result = sessionService.getScheduleSessions(null, null, null, pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findAvailableSessionsWithMovieAndHall(eq(CinemaSessionStatus.SCHEDULED),
				eq(pageable));
	}

	@Test
	void getTodayPublicSessions() {
		SessionScheduleResponse response = SessionScheduleResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Session> page = new PageImpl<>(List.of(testSession));

		when(sessionRepository.findByStartTimeBetweenWithMovieAndHall(any(LocalDateTime.class),
				any(LocalDateTime.class), eq(false), eq(CinemaSessionStatus.SCHEDULED), eq(pageable))).thenReturn(page);
		when(sessionMapper.toSessionScheduleResponse(testSession)).thenReturn(response);

		Page<SessionScheduleResponse> result = sessionService.getTodayPublicSessions(pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findByStartTimeBetweenWithMovieAndHall(any(LocalDateTime.class),
				any(LocalDateTime.class), eq(false), eq(CinemaSessionStatus.SCHEDULED), eq(pageable));
	}

	@Test
	void hasTimeConflict_WhenConflictExists() {
		when(sessionRepository.existsConflictingSession(anyLong(), any(), any(), any())).thenReturn(true);

		boolean result = sessionService.hasTimeConflict(1L, testStartTime, 120, null);

		assertThat(result).isTrue();
	}

	@Test
	void hasTimeConflict_WhenNoConflict() {
		when(sessionRepository.existsConflictingSession(anyLong(), any(), any(), any())).thenReturn(false);

		boolean result = sessionService.hasTimeConflict(1L, testStartTime, 120, null);

		assertThat(result).isFalse();
	}

	@Test
	void findSessionsToStart_ShouldUseCurrentTime() {
		List<Session> sessions = List.of(testSession);

		when(sessionRepository.findSessionsToStart(any(LocalDateTime.class), eq(CinemaSessionStatus.SCHEDULED)))
				.thenReturn(sessions);

		List<Session> result = sessionService.findSessionsToStart();

		assertThat(result).hasSize(1);
		verify(sessionRepository).findSessionsToStart(any(LocalDateTime.class), eq(CinemaSessionStatus.SCHEDULED));
	}

	@Test
	void findSessionsToComplete_ShouldUseCurrentTime() {
		List<Session> sessions = List.of(testSession);

		when(sessionRepository.findSessionsToComplete(any(LocalDateTime.class))).thenReturn(sessions);

		List<Session> result = sessionService.findSessionsToComplete();

		assertThat(result).hasSize(1);
		verify(sessionRepository).findSessionsToComplete(any(LocalDateTime.class));
	}

	@Test
	void getSessionsForAdmin_WithStatusAndSearch() {
		Session session1 = new Session();
		session1.setId(1L);
		session1.setMovie(testMovie);
		session1.setHall(testHall);
		session1.setStatus(CinemaSessionStatus.SCHEDULED);
		session1.setStartTime(testStartTime);

		Session session2 = new Session();
		session2.setId(2L);
		session2.setMovie(testMovie);
		session2.setHall(testHall);
		session2.setStatus(CinemaSessionStatus.COMPLETED);
		session2.setStartTime(testStartTime.plusHours(1));

		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Session> page = new PageImpl<>(List.of(session1, session2));

		when(sessionRepository.findByMovieTitleWithMovieAndHall(eq("test"), eq(true), eq(CinemaSessionStatus.SCHEDULED),
				eq(pageable))).thenReturn(page);
		when(sessionMapper.toSessionAdminResponse(session1)).thenReturn(response);

		Page<SessionAdminResponse> result = sessionService.getSessionsForAdmin("test", null, null, null,
				CinemaSessionStatus.SCHEDULED, pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findByMovieTitleWithMovieAndHall(eq("test"), eq(true),
				eq(CinemaSessionStatus.SCHEDULED), eq(pageable));
	}

	@Test
	void getSessionsForAdmin_WithMultipleFilters() {
		Movie otherMovie = new Movie();
		otherMovie.setId(2L);
		otherMovie.setTitle("Other Movie");

		Session session1 = new Session();
		session1.setId(1L);
		session1.setMovie(testMovie);
		session1.setHall(testHall);
		session1.setStatus(CinemaSessionStatus.SCHEDULED);
		session1.setStartTime(LocalDate.now().atTime(14, 0));

		Session session2 = new Session();
		session2.setId(2L);
		session2.setMovie(otherMovie);
		session2.setHall(testHall);
		session2.setStatus(CinemaSessionStatus.SCHEDULED);
		session2.setStartTime(LocalDate.now().atTime(16, 0));

		Session session3 = new Session();
		session3.setId(3L);
		session3.setMovie(testMovie);
		session3.setHall(testHall);
		session3.setStatus(CinemaSessionStatus.CANCELLED);
		session3.setStartTime(LocalDate.now().atTime(18, 0));

		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Session> page = new PageImpl<>(List.of(session1, session2, session3));

		when(sessionRepository.findByMovieTitleWithMovieAndHall(eq("test"), eq(true), eq(CinemaSessionStatus.SCHEDULED),
				eq(pageable))).thenReturn(page);
		when(sessionMapper.toSessionAdminResponse(session1)).thenReturn(response);

		Page<SessionAdminResponse> result = sessionService.getSessionsForAdmin("test", LocalDate.now(), 1L, 1L,
				CinemaSessionStatus.SCHEDULED, pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findByMovieTitleWithMovieAndHall(eq("test"), eq(true),
				eq(CinemaSessionStatus.SCHEDULED), eq(pageable));
	}
}