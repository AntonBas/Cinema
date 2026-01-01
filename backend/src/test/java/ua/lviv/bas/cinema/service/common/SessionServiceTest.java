package ua.lviv.bas.cinema.service.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import ua.lviv.bas.cinema.exception.domain.cinema.SessionTimeConflictException;
import ua.lviv.bas.cinema.mapper.SessionMapper;
import ua.lviv.bas.cinema.repository.MovieRepository;
import ua.lviv.bas.cinema.repository.SessionRepository;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

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

	private Movie movie;
	private CinemaHall hall;
	private Session session;
	private SessionCreateRequest createRequest;
	private SessionAdminResponse adminResponse;
	private SessionScheduleResponse scheduleResponse;

	@BeforeEach
	void setUp() {
		movie = Movie.builder().id(1L).title("Test Movie").durationMinutes(120)
				.releaseDate(LocalDate.now().minusDays(1)).build();

		hall = CinemaHall.builder().id(1L).name("Test Hall").seats(List.of()).build();

		LocalDateTime startTime = LocalDateTime.now().plusHours(2);
		session = Session.builder().id(1L).startTime(startTime).basePrice(BigDecimal.valueOf(250)).movie(movie)
				.hall(hall).status(CinemaSessionStatus.SCHEDULED).build();

		createRequest = SessionCreateRequest.builder().startTime(startTime).basePrice(BigDecimal.valueOf(250))
				.movieId(1L).hallId(1L).build();

		adminResponse = SessionAdminResponse.builder().id(1L).startTime(startTime).basePrice(BigDecimal.valueOf(250))
				.status(CinemaSessionStatus.SCHEDULED).build();

		scheduleResponse = SessionScheduleResponse.builder().id(1L).startTime(startTime)
				.basePrice(BigDecimal.valueOf(250)).status(CinemaSessionStatus.SCHEDULED).availableSeats(100).build();
	}

	@Test
	void createSession_ShouldCreateSuccessfully() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(cinemaHallService.getHallEntityById(1L)).thenReturn(hall);
		when(sessionRepository.findConflictingSessions(any(), any(), any(), any())).thenReturn(List.of());
		when(sessionMapper.toEntity(createRequest)).thenReturn(session);
		when(sessionRepository.save(session)).thenReturn(session);
		when(sessionMapper.toAdminDto(session)).thenReturn(adminResponse);

		SessionAdminResponse result = sessionService.createSession(createRequest);

		assertThat(result).isNotNull();
		verify(sessionRepository).save(session);
	}

	@Test
	void createSession_ShouldThrowException_WhenMovieNotFound() {
		when(movieRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> sessionService.createSession(createRequest))
				.isInstanceOf(MovieNotFoundException.class);
	}

	@Test
	void createSession_ShouldThrowException_WhenTimeConflict() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(cinemaHallService.getHallEntityById(1L)).thenReturn(hall);
		when(sessionRepository.findConflictingSessions(any(), any(), any(), any())).thenReturn(List.of(session));

		assertThatThrownBy(() -> sessionService.createSession(createRequest))
				.isInstanceOf(SessionTimeConflictException.class);
	}

	@Test
	void getSessionById_ShouldReturnSession() {
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(sessionMapper.toAdminDto(session)).thenReturn(adminResponse);

		SessionAdminResponse result = sessionService.getSessionById(1L);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
	}

	@Test
	void getSessionById_ShouldThrowException_WhenNotFound() {
		when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> sessionService.getSessionById(1L)).isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void getSessionByIdForPublic_ShouldReturnSession() {
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(sessionMapper.toScheduleDto(session)).thenReturn(scheduleResponse);

		SessionScheduleResponse result = sessionService.getSessionByIdForPublic(1L);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
	}

	@Test
	void getSessionByIdForPublic_ShouldThrowException_WhenNotScheduled() {
		session.setStatus(CinemaSessionStatus.CANCELLED);
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

		assertThatThrownBy(() -> sessionService.getSessionByIdForPublic(1L))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void getSessionByIdForPublic_ShouldThrowException_WhenPastSession() {
		session.setStartTime(LocalDateTime.now().minusHours(1));
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

		assertThatThrownBy(() -> sessionService.getSessionByIdForPublic(1L))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void updateSession_ShouldUpdateSuccessfully() {
		SessionUpdateRequest updateRequest = SessionUpdateRequest.builder().basePrice(BigDecimal.valueOf(300)).build();

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(sessionRepository.save(session)).thenReturn(session);
		when(sessionMapper.toAdminDto(session)).thenReturn(adminResponse);

		SessionAdminResponse result = sessionService.updateSession(1L, updateRequest);

		assertThat(result).isNotNull();
		verify(sessionMapper).updateEntityFromDto(updateRequest, session);
	}

	@Test
	void updateSession_ShouldThrowException_WhenNotFound() {
		SessionUpdateRequest updateRequest = SessionUpdateRequest.builder().basePrice(BigDecimal.valueOf(300)).build();

		when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> sessionService.updateSession(1L, updateRequest))
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
		when(sessionRepository.existsById(1L)).thenReturn(false);

		assertThatThrownBy(() -> sessionService.deleteSession(1L)).isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void cancelSession_ShouldCancelSuccessfully() {
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

		sessionService.cancelSession(1L);

		assertThat(session.getStatus()).isEqualTo(CinemaSessionStatus.CANCELLED);
		verify(sessionRepository).save(session);
	}

	@Test
	void cancelSession_ShouldDoNothing_WhenAlreadyCancelled() {
		session.setStatus(CinemaSessionStatus.CANCELLED);
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

		sessionService.cancelSession(1L);

		verify(sessionRepository, never()).save(any());
	}

	@Test
	void cancelSession_ShouldThrowException_WhenLessThanHourBeforeStart() {
		session.setStartTime(LocalDateTime.now().plusMinutes(30));
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

		assertThatThrownBy(() -> sessionService.cancelSession(1L)).isInstanceOf(IllegalStateException.class);
	}

	@Test
	void reactivateSession_ShouldReactivateSuccessfully() {
		session.setStatus(CinemaSessionStatus.CANCELLED);
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(sessionRepository.findConflictingSessions(any(), any(), any(), any())).thenReturn(List.of());

		sessionService.reactivateSession(1L);

		assertThat(session.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
		verify(sessionRepository).save(session);
	}

	@Test
	void reactivateSession_ShouldThrowException_WhenTimeConflict() {
		session.setStatus(CinemaSessionStatus.CANCELLED);
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(sessionRepository.findConflictingSessions(any(), any(), any(), any())).thenReturn(List.of(session));

		assertThatThrownBy(() -> sessionService.reactivateSession(1L)).isInstanceOf(SessionTimeConflictException.class);
	}

	@Test
	void getSessionsForAdmin_ShouldReturnAllSessions() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(pageable)).thenReturn(sessionPage);
		when(sessionMapper.toAdminDto(session)).thenReturn(adminResponse);

		Page<SessionAdminResponse> result = sessionService.getSessionsForAdmin(null, null, null, null, null, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void getScheduleSessions_ShouldReturnAvailableSessions() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAvailableSessions(pageable)).thenReturn(sessionPage);
		when(sessionMapper.toScheduleDto(session)).thenReturn(scheduleResponse);

		Page<SessionScheduleResponse> result = sessionService.getScheduleSessions(null, null, null, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void hasTimeConflict_ShouldReturnTrue_WhenConflictExists() {
		when(sessionRepository.findConflictingSessions(any(), any(), any(), any())).thenReturn(List.of(session));

		boolean result = sessionService.hasTimeConflict(1L, LocalDateTime.now(), 120, null);

		assertThat(result).isTrue();
	}

	@Test
	void hasTimeConflict_ShouldReturnFalse_WhenNoConflict() {
		when(sessionRepository.findConflictingSessions(any(), any(), any(), any())).thenReturn(List.of());

		boolean result = sessionService.hasTimeConflict(1L, LocalDateTime.now(), 120, null);

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

		assertThatThrownBy(() -> sessionService.getEndTime(session)).isInstanceOf(IllegalStateException.class);
	}
}