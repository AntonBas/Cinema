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
import java.util.Collections;
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
		when(sessionRepository.findConflictingSessions(anyLong(), any(), any(), any()))
				.thenReturn(Collections.emptyList());
		when(sessionMapper.toSession(request)).thenReturn(testSession);
		when(sessionRepository.save(testSession)).thenReturn(testSession);
		when(sessionMapper.toSessionAdminResponse(testSession)).thenReturn(response);
		when(sessionRepository.findConflictingSessions(anyLong(), any(), any(), any()))
				.thenReturn(Collections.emptyList());

		SessionAdminResponse result = sessionService.createSession(request);

		assertThat(result).isNotNull();
		verify(sessionRepository).save(testSession);
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

		Session conflictingSession = new Session();
		conflictingSession.setId(2L);

		when(movieRepository.findById(1L)).thenReturn(Optional.of(testMovie));
		when(cinemaHallService.getHallEntityById(1L)).thenReturn(testHall);
		when(sessionRepository.findConflictingSessions(anyLong(), any(), any(), any()))
				.thenReturn(List.of(conflictingSession));

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
	void updateSession_Success() {
		SessionUpdateRequest request = SessionUpdateRequest.builder().basePrice(BigDecimal.valueOf(300)).build();

		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
		doNothing().when(sessionMapper).updateSessionFromRequest(request, testSession);
		when(sessionRepository.save(testSession)).thenReturn(testSession);
		when(sessionMapper.toSessionAdminResponse(testSession)).thenReturn(response);

		SessionAdminResponse result = sessionService.updateSession(1L, request);

		assertThat(result).isNotNull();
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
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
		when(sessionRepository.save(testSession)).thenReturn(testSession);

		sessionService.cancelSession(1L);

		assertThat(testSession.getStatus()).isEqualTo(CinemaSessionStatus.CANCELLED);
		verify(sessionRepository).save(testSession);
	}

	@Test
	void cancelSession_WhenAlreadyCancelled_ShouldDoNothing() {
		testSession.setStatus(CinemaSessionStatus.CANCELLED);

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));

		sessionService.cancelSession(1L);

		verify(sessionRepository, never()).save(any());
	}

	@Test
	void reactivateSession_Success() {
		testSession.setStatus(CinemaSessionStatus.CANCELLED);

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
		when(sessionRepository.findConflictingSessions(anyLong(), any(), any(), any()))
				.thenReturn(Collections.emptyList());
		when(sessionRepository.save(testSession)).thenReturn(testSession);

		sessionService.reactivateSession(1L);

		assertThat(testSession.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
		verify(sessionRepository).save(testSession);
	}

	@Test
	void reactivateSession_WhenTimeConflict_ShouldThrowException() {
		testSession.setStatus(CinemaSessionStatus.CANCELLED);

		Session conflictingSession = new Session();
		conflictingSession.setId(2L);

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
		when(sessionRepository.findConflictingSessions(anyLong(), any(), any(), any()))
				.thenReturn(List.of(conflictingSession));

		assertThatThrownBy(() -> sessionService.reactivateSession(1L)).isInstanceOf(SessionTimeConflictException.class);
	}

	@Test
	void getSessionsForAdmin_WithSearch() {
		SessionAdminResponse response = SessionAdminResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Session> page = new PageImpl<>(List.of(testSession));

		when(sessionRepository.findByMovieTitle("test", true, pageable)).thenReturn(page);
		when(sessionMapper.toSessionAdminResponse(testSession)).thenReturn(response);

		Page<SessionAdminResponse> result = sessionService.getSessionsForAdmin("test", null, null, null, null,
				pageable);

		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void getScheduleSessions_WithDate() {
		SessionScheduleResponse response = SessionScheduleResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Session> page = new PageImpl<>(List.of(testSession));

		when(sessionRepository.findByStartTimeBetween(any(), any(), eq(false), any())).thenReturn(page);
		when(sessionMapper.toSessionScheduleResponse(testSession)).thenReturn(response);

		Page<SessionScheduleResponse> result = sessionService.getScheduleSessions(LocalDate.now(), null, null,
				pageable);

		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void hasTimeConflict_WhenConflictExists() {
		Session conflictingSession = new Session();

		when(sessionRepository.findConflictingSessions(anyLong(), any(), any(), any()))
				.thenReturn(List.of(conflictingSession));

		boolean result = sessionService.hasTimeConflict(1L, testStartTime, 120, null);

		assertThat(result).isTrue();
	}

	@Test
	void hasTimeConflict_WhenNoConflict() {
		when(sessionRepository.findConflictingSessions(anyLong(), any(), any(), any()))
				.thenReturn(Collections.emptyList());

		boolean result = sessionService.hasTimeConflict(1L, testStartTime, 120, null);

		assertThat(result).isFalse();
	}

	@Test
	void getEndTime_Success() {
		LocalDateTime endTime = sessionService.getEndTime(testSession);

		assertThat(endTime).isEqualTo(testStartTime.plusMinutes(120));
	}

	@Test
	void getEndTime_WhenMissingData_ShouldThrowException() {
		testSession.setStartTime(null);

		assertThatThrownBy(() -> sessionService.getEndTime(testSession)).isInstanceOf(SessionOperationException.class);
	}
}