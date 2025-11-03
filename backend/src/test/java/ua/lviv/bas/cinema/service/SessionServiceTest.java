package ua.lviv.bas.cinema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.dto.SessionDto;
import ua.lviv.bas.cinema.dto.SessionRequest;
import ua.lviv.bas.cinema.exception.ConflictException;
import ua.lviv.bas.cinema.exception.SessionNotFoundException;
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

	@Test
	void createSession_ShouldCreateSuccessfully() {
		SessionRequest request = SessionRequest.builder().startTime(LocalDateTime.now().plusHours(2))
				.price(new BigDecimal("250.00")).movieId(1L).hallId(1L).build();

		Movie movie = Movie.builder().id(1L).durationMinutes(120).build();
		CinemaHall hall = CinemaHall.builder().id(1L).build();
		Session session = Session.builder().id(1L).build();
		SessionDto sessionDto = SessionDto.builder().id(1L).build();

		when(movieService.getMovieEntityById(1L)).thenReturn(movie);
		when(cinemaHallService.getHallEntityById(1L)).thenReturn(hall);
		when(sessionRepository.findConflictingSessions(any(), any(), any(), any())).thenReturn(List.of());
		when(sessionMapper.toEntity(request)).thenReturn(session);
		when(sessionRepository.save(session)).thenReturn(session);
		when(sessionMapper.toDto(session)).thenReturn(sessionDto);

		SessionDto result = sessionService.createSession(request);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		verify(sessionRepository).save(session);
	}

	@Test
	void createSession_ShouldThrowConflictException_WhenTimeConflict() {
		SessionRequest request = SessionRequest.builder().startTime(LocalDateTime.now().plusHours(2)).movieId(1L)
				.hallId(1L).build();

		Movie movie = Movie.builder().id(1L).durationMinutes(120).build();
		CinemaHall hall = CinemaHall.builder().id(1L).build();
		Session conflictingSession = Session.builder().id(2L).build();

		when(movieService.getMovieEntityById(1L)).thenReturn(movie);
		when(cinemaHallService.getHallEntityById(1L)).thenReturn(hall);
		when(sessionRepository.findConflictingSessions(any(), any(), any(), any()))
				.thenReturn(List.of(conflictingSession));

		assertThatThrownBy(() -> sessionService.createSession(request)).isInstanceOf(ConflictException.class)
				.hasMessageContaining("Time conflict");
	}

	@Test
	void getSessionById_ShouldReturnSession() {
		Session session = Session.builder().id(1L).build();
		SessionDto sessionDto = SessionDto.builder().id(1L).build();

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(sessionMapper.toDto(session)).thenReturn(sessionDto);

		SessionDto result = sessionService.getSessionById(1L);

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
		SessionRequest request = SessionRequest.builder().startTime(LocalDateTime.now().plusHours(3))
				.price(new BigDecimal("300.00")).movieId(1L).hallId(1L).build();

		Session existingSession = Session.builder().id(1L).build();
		Movie movie = Movie.builder().id(1L).durationMinutes(120).build();
		CinemaHall hall = CinemaHall.builder().id(1L).build();
		SessionDto sessionDto = SessionDto.builder().id(1L).build();

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(existingSession));
		when(movieService.getMovieEntityById(1L)).thenReturn(movie);
		when(cinemaHallService.getHallEntityById(1L)).thenReturn(hall);
		when(sessionRepository.findConflictingSessions(any(), any(), any(), any())).thenReturn(List.of());
		when(sessionRepository.save(existingSession)).thenReturn(existingSession);
		when(sessionMapper.toDto(existingSession)).thenReturn(sessionDto);

		SessionDto result = sessionService.updateSession(1L, request);

		assertThat(result).isNotNull();
		verify(sessionRepository).save(existingSession);
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
	void getAllSessions_ShouldReturnAllSessions() {
		Session session = Session.builder().id(1L).build();
		SessionDto sessionDto = SessionDto.builder().id(1L).build();

		when(sessionRepository.findAll()).thenReturn(List.of(session));
		when(sessionMapper.toDto(session)).thenReturn(sessionDto);

		List<SessionDto> result = sessionService.getAllSessions();

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getId()).isEqualTo(1L);
	}
}