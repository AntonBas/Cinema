package ua.lviv.bas.cinema.service.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.filter.SessionFilter;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionUpdateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionTimeConflictException;
import ua.lviv.bas.cinema.mapper.SessionMapper;
import ua.lviv.bas.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.service.query.SessionQueryService;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

	@Mock
	private SessionRepository sessionRepository;
	@Mock
	private SessionQueryService sessionQueryService;
	@Mock
	private SessionMapper sessionMapper;
	@Mock
	private MovieService movieService;
	@Mock
	private CinemaHallService cinemaHallService;
	@InjectMocks
	private SessionService sessionService;

	private SessionCreateRequest sessionRequest;
	private SessionUpdateRequest sessionUpdateRequest;
	private Movie movie;
	private CinemaHall hall;
	private Session session;
	private Session sessionPast;
	private SessionAdminResponse sessionAdminDto;
	private SessionScheduleResponse sessionScheduleDto;
	private Pageable pageable;

	@BeforeEach
	void setUp() {
		sessionRequest = SessionCreateRequest.builder().startTime(LocalDateTime.now().plusHours(2))
				.basePrice(new BigDecimal("250.00")).movieId(1L).hallId(1L).build();

		sessionUpdateRequest = SessionUpdateRequest.builder().basePrice(new BigDecimal("300.00"))
				.status(CinemaSessionStatus.COMPLETED).build();

		movie = new Movie();
		movie.setId(1L);
		movie.setDurationMinutes(120);
		movie.setTitle("Test Movie");
		movie.setReleaseDate(LocalDate.now().minusDays(1));
		movie.setEndShowingDate(LocalDate.now().plusDays(30));

		hall = new CinemaHall();
		hall.setId(1L);
		hall.setName("Hall 1");
		hall.setSeats(new ArrayList<>());
		for (int i = 1; i <= 100; i++) {
			Seat seat = new Seat();
			seat.setId((long) i);
			seat.setHall(hall);
			hall.getSeats().add(seat);
		}

		LocalDateTime startTime = LocalDateTime.now().plusHours(2);
		session = new Session();
		session.setId(1L);
		session.setStartTime(startTime);
		session.setBasePrice(new BigDecimal("250.00"));
		session.setMovie(movie);
		session.setHall(hall);
		session.setStatus(CinemaSessionStatus.SCHEDULED);

		sessionPast = new Session();
		sessionPast.setId(2L);
		sessionPast.setStartTime(LocalDateTime.now().minusHours(2));
		sessionPast.setBasePrice(new BigDecimal("250.00"));
		sessionPast.setMovie(movie);
		sessionPast.setHall(hall);
		sessionPast.setStatus(CinemaSessionStatus.SCHEDULED);

		sessionAdminDto = new SessionAdminResponse();
		sessionAdminDto.setId(1L);
		sessionAdminDto.setStartTime(startTime);
		sessionAdminDto.setBasePrice(new BigDecimal("250.00"));
		sessionAdminDto.setStatus(CinemaSessionStatus.SCHEDULED);
		sessionAdminDto.setEndTime(startTime.plusMinutes(120));
		sessionAdminDto.setHallCapacity(100);

		sessionScheduleDto = new SessionScheduleResponse();
		sessionScheduleDto.setId(1L);
		sessionScheduleDto.setStartTime(startTime);
		sessionScheduleDto.setBasePrice(new BigDecimal("250.00"));
		sessionScheduleDto.setStatus(CinemaSessionStatus.SCHEDULED);

		pageable = PageRequest.of(0, 20);
	}

	@Test
	void createSession_ShouldCreateSuccessfully() {
		when(movieService.getMovieEntityById(1L)).thenReturn(movie);
		when(cinemaHallService.getHallEntityById(1L)).thenReturn(hall);
		when(sessionQueryService.findConflictingSessions(anyLong(), any(), any(), any())).thenReturn(List.of());
		when(sessionMapper.toEntity(sessionRequest)).thenReturn(session);
		when(sessionRepository.save(session)).thenReturn(session);
		when(sessionMapper.toAdminDto(session)).thenReturn(sessionAdminDto);

		SessionAdminResponse result = sessionService.createSession(sessionRequest);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		verify(sessionRepository).save(session);
	}

	@Test
	void createSession_ShouldThrowException_WhenTimeConflict() {
		when(movieService.getMovieEntityById(1L)).thenReturn(movie);
		when(cinemaHallService.getHallEntityById(1L)).thenReturn(hall);
		when(sessionQueryService.findConflictingSessions(anyLong(), any(), any(), any()))
				.thenReturn(List.of(new Session()));

		assertThatThrownBy(() -> sessionService.createSession(sessionRequest))
				.isInstanceOf(SessionTimeConflictException.class);
	}

	@Test
	void createSession_ShouldThrowException_WhenStartTimeTooSoon() {
		SessionCreateRequest invalidRequest = SessionCreateRequest.builder()
				.startTime(LocalDateTime.now().plusMinutes(20)).basePrice(new BigDecimal("250.00")).movieId(1L)
				.hallId(1L).build();

		assertThatThrownBy(() -> sessionService.createSession(invalidRequest))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("at least 30 minutes");
	}

	@Test
	void getSessionById_ShouldReturnSession() {
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(sessionMapper.toAdminDto(session)).thenReturn(sessionAdminDto);

		SessionAdminResponse result = sessionService.getSessionById(1L);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
	}

	@Test
	void getSessionById_ShouldThrowException_WhenNotFound() {
		when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> sessionService.getSessionById(99L)).isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void getSessionByIdForPublic_ShouldReturnSession() {
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(sessionMapper.toScheduleDto(session)).thenReturn(sessionScheduleDto);

		SessionScheduleResponse result = sessionService.getSessionByIdForPublic(1L);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
	}

	@Test
	void getSessionByIdForPublic_ShouldThrowException_WhenSessionNotAvailable() {
		session.setStatus(CinemaSessionStatus.COMPLETED);
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

		assertThatThrownBy(() -> sessionService.getSessionByIdForPublic(1L))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void getSessionByIdForPublic_ShouldThrowException_WhenSessionPast() {
		session.setStartTime(LocalDateTime.now().minusHours(1));
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

		assertThatThrownBy(() -> sessionService.getSessionByIdForPublic(1L))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void getSessionByIdForPublic_ShouldThrowException_WhenNotFound() {
		when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> sessionService.getSessionByIdForPublic(99L))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void updateSession_ShouldUpdateSuccessfully() {
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(sessionRepository.save(any(Session.class))).thenReturn(session);
		when(sessionMapper.toAdminDto(session)).thenReturn(sessionAdminDto);

		SessionAdminResponse result = sessionService.updateSession(1L, sessionUpdateRequest);

		assertThat(result).isNotNull();
		verify(sessionMapper).updateEntityFromDto(sessionUpdateRequest, session);
	}

	@Test
	void updateSession_ShouldValidateTimeConflict_WhenStartTimeChanged() {
		SessionUpdateRequest updateRequest = SessionUpdateRequest.builder().startTime(LocalDateTime.now().plusHours(3))
				.build();

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(sessionQueryService.findConflictingSessions(anyLong(), any(), any(), any())).thenReturn(List.of());
		when(sessionRepository.save(any(Session.class))).thenReturn(session);
		when(sessionMapper.toAdminDto(session)).thenReturn(sessionAdminDto);

		SessionAdminResponse result = sessionService.updateSession(1L, updateRequest);

		assertThat(result).isNotNull();
		verify(sessionQueryService).findConflictingSessions(anyLong(), any(), any(), any());
	}

	@Test
	void updateSession_ShouldThrowException_WhenTimeConflict() {
		SessionUpdateRequest updateRequest = SessionUpdateRequest.builder().startTime(LocalDateTime.now().plusHours(3))
				.build();

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(sessionQueryService.findConflictingSessions(anyLong(), any(), any(), any()))
				.thenReturn(List.of(new Session()));

		assertThatThrownBy(() -> sessionService.updateSession(1L, updateRequest))
				.isInstanceOf(SessionTimeConflictException.class);
	}

	@Test
	void updateSession_ShouldThrowException_WhenNotFound() {
		when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> sessionService.updateSession(99L, sessionUpdateRequest))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void updateSessionStatus_ShouldUpdateStatusSuccessfully() {
		Session pastSession = new Session();
		pastSession.setId(3L);
		pastSession.setStartTime(LocalDateTime.now().minusHours(2));
		pastSession.setMovie(movie);
		pastSession.setHall(hall);
		pastSession.setStatus(CinemaSessionStatus.SCHEDULED);

		SessionAdminResponse pastAdminDto = new SessionAdminResponse();
		pastAdminDto.setId(3L);
		pastAdminDto.setStartTime(pastSession.getStartTime());
		pastAdminDto.setEndTime(pastSession.getStartTime().plusMinutes(120));
		pastAdminDto.setStatus(CinemaSessionStatus.COMPLETED);

		when(sessionRepository.findById(3L)).thenReturn(Optional.of(pastSession));
		when(sessionRepository.save(any(Session.class))).thenReturn(pastSession);
		when(sessionMapper.toAdminDto(pastSession)).thenReturn(pastAdminDto);

		SessionAdminResponse result = sessionService.updateSessionStatus(3L, CinemaSessionStatus.COMPLETED);

		assertThat(result).isNotNull();
		assertThat(result.getStatus()).isEqualTo(CinemaSessionStatus.COMPLETED);
		verify(sessionRepository).save(pastSession);
	}

	@Test
	void updateSessionStatus_ShouldThrowException_WhenSessionCompleted() {
		session.setStatus(CinemaSessionStatus.COMPLETED);
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

		assertThatThrownBy(() -> sessionService.updateSessionStatus(1L, CinemaSessionStatus.SCHEDULED))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("completed session");
	}

	@Test
	void updateSessionStatus_ShouldThrowException_WhenCancellingTooLate() {
		session.setStartTime(LocalDateTime.now().plusMinutes(30));
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

		assertThatThrownBy(() -> sessionService.updateSessionStatus(1L, CinemaSessionStatus.CANCELLED))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("less than 1 hour");
	}

	@Test
	void updateSessionStatus_ShouldThrowException_WhenCompletingFutureSession() {
		session.setStartTime(LocalDateTime.now().plusHours(2));
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

		assertThatThrownBy(() -> sessionService.updateSessionStatus(1L, CinemaSessionStatus.COMPLETED))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("future session");
	}

	@Test
	void updateSessionStatus_ShouldThrowException_WhenNotFound() {
		when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> sessionService.updateSessionStatus(99L, CinemaSessionStatus.COMPLETED))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void deleteSession_ShouldDeleteSuccessfully() {
		when(sessionRepository.existsById(1L)).thenReturn(true);

		sessionService.deleteSession(1L);

		verify(sessionRepository).deleteById(1L);
	}

	@Test
	void deleteSession_ShouldThrowException_WhenNotFound() {
		when(sessionRepository.existsById(99L)).thenReturn(false);

		assertThatThrownBy(() -> sessionService.deleteSession(99L)).isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void getAllSessionsForAdmin_ShouldReturnSessions() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionQueryService.findByMovieTitle(null, pageable, true)).thenReturn(sessionPage);
		when(sessionMapper.toAdminDto(session)).thenReturn(sessionAdminDto);

		Page<SessionAdminResponse> result = sessionService.getAllSessionsForAdmin(pageable, null);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void getSessionsByDateForAdmin_ShouldReturnSessions() {
		LocalDate date = LocalDate.of(2024, 1, 15);
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionQueryService.findByStartTimeBetween(any(), any(), any(), anyBoolean())).thenReturn(sessionPage);
		when(sessionMapper.toAdminDto(session)).thenReturn(sessionAdminDto);

		Page<SessionAdminResponse> result = sessionService.getSessionsByDateForAdmin(date, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void getSessionsByHallForAdmin_ShouldReturnSessions() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionQueryService.findByHallId(1L, pageable, true)).thenReturn(sessionPage);
		when(sessionMapper.toAdminDto(session)).thenReturn(sessionAdminDto);

		Page<SessionAdminResponse> result = sessionService.getSessionsByHallForAdmin(1L, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void getSessionsByMovieForAdmin_ShouldReturnSessions() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionQueryService.findByMovieId(1L, pageable, true)).thenReturn(sessionPage);
		when(sessionMapper.toAdminDto(session)).thenReturn(sessionAdminDto);

		Page<SessionAdminResponse> result = sessionService.getSessionsByMovieForAdmin(1L, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void getSessionsByStatus_ShouldReturnSessions() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionQueryService.findByStatus(CinemaSessionStatus.SCHEDULED, pageable)).thenReturn(sessionPage);
		when(sessionMapper.toAdminDto(session)).thenReturn(sessionAdminDto);

		Page<SessionAdminResponse> result = sessionService.getSessionsByStatus(CinemaSessionStatus.SCHEDULED, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void getAvailableSessionsForAdmin_ShouldReturnSessions() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionQueryService.findAvailableSessions(pageable)).thenReturn(sessionPage);
		when(sessionMapper.toAdminDto(session)).thenReturn(sessionAdminDto);

		Page<SessionAdminResponse> result = sessionService.getAvailableSessionsForAdmin(pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void getFilteredSessions_ShouldReturnSessions() {
		SessionFilter filter = SessionFilter.builder().adminView(true).build();
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionQueryService.findFilteredSessions(filter)).thenReturn(sessionPage);
		when(sessionMapper.toAdminDto(session)).thenReturn(sessionAdminDto);

		Page<SessionAdminResponse> result = sessionService.getFilteredSessions(filter);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void getScheduleSessions_ShouldReturnSessions() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionQueryService.findAvailableSessions(pageable)).thenReturn(sessionPage);
		when(sessionMapper.toScheduleDto(session)).thenReturn(sessionScheduleDto);

		Page<SessionScheduleResponse> result = sessionService.getScheduleSessions(pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void getScheduleSessionsByDate_ShouldReturnSessions() {
		LocalDate date = LocalDate.of(2024, 1, 15);
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionQueryService.findByStartTimeBetween(any(), any(), any(), anyBoolean())).thenReturn(sessionPage);
		when(sessionMapper.toScheduleDto(session)).thenReturn(sessionScheduleDto);

		Page<SessionScheduleResponse> result = sessionService.getScheduleSessionsByDate(date, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void getScheduleSessionsByMovie_ShouldReturnSessions() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionQueryService.findByMovieId(1L, pageable, false)).thenReturn(sessionPage);
		when(sessionMapper.toScheduleDto(session)).thenReturn(sessionScheduleDto);

		Page<SessionScheduleResponse> result = sessionService.getScheduleSessionsByMovie(1L, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void getUpcomingScheduleSessions_ShouldReturnSessions() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionQueryService.findByStartTimeBetween(any(), any(), any(), anyBoolean())).thenReturn(sessionPage);
		when(sessionMapper.toScheduleDto(session)).thenReturn(sessionScheduleDto);

		Page<SessionScheduleResponse> result = sessionService.getUpcomingScheduleSessions(7, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void hasTimeConflict_ShouldReturnTrue_WhenConflictExists() {
		LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 15, 18, 0);
		Session conflictingSession = new Session();
		conflictingSession.setId(2L);

		when(sessionQueryService.findConflictingSessions(1L, fixedTime, fixedTime.plusMinutes(120), null))
				.thenReturn(List.of(conflictingSession));

		boolean result = sessionService.hasTimeConflict(1L, fixedTime, 120, null);

		assertThat(result).isTrue();
	}

	@Test
	void hasTimeConflict_ShouldReturnFalse_WhenNoConflict() {
		LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 15, 18, 0);

		when(sessionQueryService.findConflictingSessions(1L, fixedTime, fixedTime.plusMinutes(120), null))
				.thenReturn(List.of());

		boolean result = sessionService.hasTimeConflict(1L, fixedTime, 120, null);

		assertThat(result).isFalse();
	}

	@Test
	void getEndTime_ShouldCalculateEndTime() {
		LocalDateTime endTime = sessionService.getEndTime(session);

		assertThat(endTime).isEqualTo(session.getStartTime().plusMinutes(120));
	}

	@Test
	void getEndTime_ShouldThrowException_WhenMissingData() {
		session.setStartTime(null);

		assertThatThrownBy(() -> sessionService.getEndTime(session)).isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("missing data");
	}

	@Test
	void isAvailable_ShouldReturnTrue_WhenSessionIsScheduledAndInFuture() {
		boolean available = sessionService.isAvailable(session);

		assertThat(available).isTrue();
	}

	@Test
	void isAvailable_ShouldReturnFalse_WhenSessionIsOngoing() {
		session.setStatus(CinemaSessionStatus.ONGOING);
		boolean available = sessionService.isAvailable(session);

		assertThat(available).isFalse();
	}

	@Test
	void isAvailable_ShouldReturnFalse_WhenSessionIsPast() {
		session.setStartTime(LocalDateTime.now().minusHours(1));
		boolean available = sessionService.isAvailable(session);

		assertThat(available).isFalse();
	}

	@Test
	void isAvailable_ShouldReturnFalse_WhenSessionIsCancelled() {
		session.setStatus(CinemaSessionStatus.CANCELLED);
		boolean available = sessionService.isAvailable(session);

		assertThat(available).isFalse();
	}

	@Test
	void isAvailable_ShouldReturnFalse_WhenSessionIsNull() {
		boolean available = sessionService.isAvailable(null);

		assertThat(available).isFalse();
	}

	@Test
	void toAdminResponse_ShouldMapWithCalculatedValues() {
		when(sessionMapper.toAdminDto(session)).thenReturn(sessionAdminDto);

		SessionAdminResponse result = sessionService.toAdminResponse(session);

		assertThat(result).isNotNull();
		assertThat(result.getEndTime()).isEqualTo(session.getStartTime().plusMinutes(120));
		assertThat(result.getHallCapacity()).isEqualTo(100);
		assertThat(result.getTotalRevenue()).isNotNull();
	}

	@Test
	void toScheduleResponse_ShouldMapWithCalculatedValues() {
		when(sessionMapper.toScheduleDto(session)).thenReturn(sessionScheduleDto);

		SessionScheduleResponse result = sessionService.toScheduleResponse(session);

		assertThat(result).isNotNull();
		assertThat(result.getEndTime()).isEqualTo(session.getStartTime().plusMinutes(120));
		assertThat(result.getHallCapacity()).contains("100");
	}
}