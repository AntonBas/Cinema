package ua.lviv.bas.cinema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import ua.lviv.bas.cinema.dto.session.request.SessionRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionResponse;
import ua.lviv.bas.cinema.exception.core.ConflictException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.mapper.SessionMapper;
import ua.lviv.bas.cinema.repository.SessionRepository;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

	@Mock
	private SessionRepository sessionRepository;

	@Mock
	private SessionMapper sessionMapper;

	@Mock
	private MovieService movieService;

	@Mock
	private CinemaHallService cinemaHallService;

	@InjectMocks
	private SessionService sessionService;

	private SessionRequest sessionRequest;
	private Movie movie;
	private CinemaHall hall;
	private Session session;
	private SessionResponse sessionDto;
	private Pageable pageable;

	@BeforeEach
	void setUp() {
		sessionRequest = SessionRequest.builder().startTime(LocalDateTime.now().plusHours(2))
				.price(new BigDecimal("250.00")).movieId(1L).hallId(1L).build();

		movie = Movie.builder().id(1L).durationMinutes(120).title("Test Movie")
				.releaseDate(LocalDate.now().minusDays(1)).endShowingDate(LocalDate.now().plusDays(30)).build();

		hall = CinemaHall.builder().id(1L).build();

		session = Session.builder().id(1L).build();

		sessionDto = SessionResponse.builder().id(1L).build();

		pageable = PageRequest.of(0, 20);
	}

	@Test
	void createSession_ShouldCreateSuccessfully() {
		when(movieService.getMovieEntityById(1L)).thenReturn(movie);
		when(cinemaHallService.getHallEntityById(1L)).thenReturn(hall);
		when(sessionRepository.findConflictingSessions(any(), any(), any(), any())).thenReturn(List.of());
		when(sessionMapper.toEntity(sessionRequest)).thenReturn(session);
		when(sessionRepository.save(session)).thenReturn(session);
		when(sessionMapper.toDto(session)).thenReturn(sessionDto);

		SessionResponse result = sessionService.createSession(sessionRequest);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		verify(sessionRepository).save(session);
	}

	@Test
	void createSession_ShouldThrowConflictException_WhenTimeConflict() {
		Session conflictingSession = Session.builder().id(2L).build();

		when(movieService.getMovieEntityById(1L)).thenReturn(movie);
		when(cinemaHallService.getHallEntityById(1L)).thenReturn(hall);
		when(sessionRepository.findConflictingSessions(any(), any(), any(), any()))
				.thenReturn(List.of(conflictingSession));

		assertThatThrownBy(() -> sessionService.createSession(sessionRequest)).isInstanceOf(ConflictException.class)
				.hasMessageContaining("Time conflict");
	}

	@Test
	void getSessionById_ShouldReturnSession() {
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(sessionMapper.toDto(session)).thenReturn(sessionDto);

		SessionResponse result = sessionService.getSessionById(1L);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
	}

	@Test
	void getSessionById_ShouldThrowException_WhenNotFound() {
		when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> sessionService.getSessionById(1L)).isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void updateSession_ShouldUpdateSuccessfully() {
		SessionRequest updateRequest = SessionRequest.builder().startTime(LocalDateTime.now().plusHours(3))
				.price(new BigDecimal("300.00")).movieId(1L).hallId(1L).build();

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(movieService.getMovieEntityById(1L)).thenReturn(movie);
		when(cinemaHallService.getHallEntityById(1L)).thenReturn(hall);
		when(sessionRepository.findConflictingSessions(any(), any(), any(), any())).thenReturn(List.of());
		when(sessionRepository.save(session)).thenReturn(session);
		when(sessionMapper.toDto(session)).thenReturn(sessionDto);

		SessionResponse result = sessionService.updateSession(1L, updateRequest);

		assertThat(result).isNotNull();
		verify(sessionRepository).save(session);
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
	void getAllSessions_ShouldReturnAllSessionsWithPagination() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findAll(pageable)).thenReturn(sessionPage);
		when(sessionMapper.toDto(session)).thenReturn(sessionDto);

		Page<SessionResponse> result = sessionService.getAllSessions(pageable, null);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
	}

	@Test
	void getAllSessions_ShouldReturnSearchedSessions() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findByMovieTitleContainingIgnoreCase("test", pageable)).thenReturn(sessionPage);
		when(sessionMapper.toDto(session)).thenReturn(sessionDto);

		Page<SessionResponse> result = sessionService.getAllSessions(pageable, "test");

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findByMovieTitleContainingIgnoreCase("test", pageable);
	}

	@Test
	void hasTimeConflict_ShouldReturnTrue_WhenConflictExists() {
		LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 15, 18, 0);
		LocalDateTime endTime = fixedTime.plusHours(2);

		Session conflictingSession = Session.builder().id(2L).build();

		when(sessionRepository.findConflictingSessions(1L, fixedTime, endTime, null))
				.thenReturn(List.of(conflictingSession));

		boolean result = sessionService.hasTimeConflict(1L, fixedTime, 120, null);

		assertThat(result).isTrue();
	}

	@Test
	void hasTimeConflict_ShouldReturnFalse_WhenNoConflict() {
		LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 15, 18, 0);
		LocalDateTime endTime = fixedTime.plusHours(2);

		when(sessionRepository.findConflictingSessions(1L, fixedTime, endTime, null)).thenReturn(List.of());

		boolean result = sessionService.hasTimeConflict(1L, fixedTime, 120, null);

		assertThat(result).isFalse();
	}

	@Test
	void getFilteredSessions_ShouldReturnFilteredSessionsByDate() {
		LocalDate date = LocalDate.of(2024, 1, 15);
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findFilteredSessions(any(), any(), any(), any(), any())).thenReturn(sessionPage);
		when(sessionMapper.toDto(session)).thenReturn(sessionDto);

		Page<SessionResponse> result = sessionService.getFilteredSessions(date, null, null, null, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findFilteredSessions(any(), any(), any(), any(), any());
	}

	@Test
	void getFilteredSessions_ShouldReturnFilteredSessionsByHallAndMovie() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findFilteredSessions(any(), any(), any(), any(), any())).thenReturn(sessionPage);
		when(sessionMapper.toDto(session)).thenReturn(sessionDto);

		Page<SessionResponse> result = sessionService.getFilteredSessions(null, 1L, 1L, null, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findFilteredSessions(any(), any(), any(), any(), any());
	}

	@Test
	void getFilteredSessions_ShouldReturnFilteredSessionsByDays() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findFilteredSessions(any(), any(), any(), any(), any())).thenReturn(sessionPage);
		when(sessionMapper.toDto(session)).thenReturn(sessionDto);

		Page<SessionResponse> result = sessionService.getFilteredSessions(null, null, null, 7, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findFilteredSessions(any(), any(), any(), any(), any());
	}

	@Test
	void getFilteredSessions_ShouldReturnAllSessions_WhenNoFilters() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findFilteredSessions(any(), any(), any(), any(), any())).thenReturn(sessionPage);
		when(sessionMapper.toDto(session)).thenReturn(sessionDto);

		Page<SessionResponse> result = sessionService.getFilteredSessions(null, null, null, null, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findFilteredSessions(any(), any(), any(), any(), any());
	}

	@Test
	void getSessionsByDate_ShouldReturnSessionsForDate() {
		LocalDate date = LocalDate.of(2024, 1, 15);
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findByStartTimeBetween(any(), any(), any())).thenReturn(sessionPage);
		when(sessionMapper.toDto(session)).thenReturn(sessionDto);

		Page<SessionResponse> result = sessionService.getSessionsByDate(date, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findByStartTimeBetween(any(), any(), any());
	}

	@Test
	void getSessionsByHall_ShouldReturnSessionsForHall() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findByHallId(1L, pageable)).thenReturn(sessionPage);
		when(sessionMapper.toDto(session)).thenReturn(sessionDto);

		Page<SessionResponse> result = sessionService.getSessionsByHall(1L, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findByHallId(1L, pageable);
	}

	@Test
	void getSessionsByMovie_ShouldReturnSessionsForMovie() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findByMovieId(1L, pageable)).thenReturn(sessionPage);
		when(sessionMapper.toDto(session)).thenReturn(sessionDto);

		Page<SessionResponse> result = sessionService.getSessionsByMovie(1L, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findByMovieId(1L, pageable);
	}

	@Test
	void getAvailableSessions_ShouldReturnAvailableSessions() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findByStartTimeAfter(any(), any())).thenReturn(sessionPage);
		when(sessionMapper.toDto(session)).thenReturn(sessionDto);

		Page<SessionResponse> result = sessionService.getAvailableSessions(pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findByStartTimeAfter(any(), any());
	}

	@Test
	void getUpcomingSessions_ShouldReturnUpcomingSessions() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findByStartTimeBetween(any(), any(), any())).thenReturn(sessionPage);
		when(sessionMapper.toDto(session)).thenReturn(sessionDto);

		Page<SessionResponse> result = sessionService.getUpcomingSessions(7, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findByStartTimeBetween(any(), any(), any());
	}

	@Test
	void getTodaySessions_ShouldReturnTodaySessions() {
		Page<Session> sessionPage = new PageImpl<>(List.of(session));

		when(sessionRepository.findByStartTimeBetween(any(), any(), any())).thenReturn(sessionPage);
		when(sessionMapper.toDto(session)).thenReturn(sessionDto);

		Page<SessionResponse> result = sessionService.getTodaySessions(pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		verify(sessionRepository).findByStartTimeBetween(any(), any(), any());
	}
}