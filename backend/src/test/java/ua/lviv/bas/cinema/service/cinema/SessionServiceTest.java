package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
	private MovieRepository movieRepository;

	@Mock
	private CinemaHallService cinemaHallService;

	@Mock
	private SessionMapper sessionMapper;

	@InjectMocks
	private SessionService sessionService;

	private final Long SESSION_ID = 1L;
	private final Long MOVIE_ID = 2L;
	private final Long HALL_ID = 3L;
	private final BigDecimal BASE_PRICE = new BigDecimal("250.00");
	private final LocalDateTime START_TIME = LocalDateTime.now().plusHours(2);

	@Test
	void createSession_Success() {
		SessionCreateRequest request = SessionCreateRequest.builder().startTime(START_TIME).basePrice(BASE_PRICE)
				.movieId(MOVIE_ID).hallId(HALL_ID).build();

		Movie movie = createMovie();
		CinemaHall hall = createCinemaHall();
		Session session = createSession();
		SessionAdminResponse response = createSessionAdminResponse();

		when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
		when(cinemaHallService.getHallEntityById(HALL_ID)).thenReturn(hall);
		when(sessionRepository.existsConflictingSession(HALL_ID, START_TIME,
				START_TIME.plusMinutes(movie.getDurationMinutes()), null)).thenReturn(false);
		when(sessionMapper.toSession(request)).thenReturn(session);
		when(sessionRepository.save(session)).thenReturn(session);
		when(sessionMapper.toSessionAdminResponse(session)).thenReturn(response);

		SessionAdminResponse result = sessionService.createSession(request);

		assertThat(result).isEqualTo(response);
		verify(sessionRepository).save(session);
	}

	@Test
	void createSession_MovieNotFound_ThrowsException() {
		SessionCreateRequest request = SessionCreateRequest.builder().startTime(START_TIME).basePrice(BASE_PRICE)
				.movieId(MOVIE_ID).hallId(HALL_ID).build();

		when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> sessionService.createSession(request)).isInstanceOf(MovieNotFoundException.class);
	}

	@Test
	void createSession_TimeConflict_ThrowsException() {
		SessionCreateRequest request = SessionCreateRequest.builder().startTime(START_TIME).movieId(MOVIE_ID)
				.hallId(HALL_ID).build();

		Movie movie = createMovie();

		when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
		when(cinemaHallService.getHallEntityById(HALL_ID)).thenReturn(createCinemaHall());
		when(sessionRepository.existsConflictingSession(HALL_ID, START_TIME,
				START_TIME.plusMinutes(movie.getDurationMinutes()), null)).thenReturn(true);

		assertThatThrownBy(() -> sessionService.createSession(request))
				.isInstanceOf(SessionTimeConflictException.class);
	}

	@Test
	void getSessionById_Success() {
		Session session = createSession();
		SessionAdminResponse response = createSessionAdminResponse();

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
		when(sessionMapper.toSessionAdminResponse(session)).thenReturn(response);

		SessionAdminResponse result = sessionService.getSessionById(SESSION_ID);

		assertThat(result).isEqualTo(response);
	}

	@Test
	void getSessionById_NotFound_ThrowsException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> sessionService.getSessionById(SESSION_ID))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void getSessionByIdForPublic_Success() {
		Session session = createSession();
		session.setStatus(CinemaSessionStatus.SCHEDULED);
		SessionScheduleResponse response = createSessionScheduleResponse();

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
		when(sessionMapper.toSessionScheduleResponse(session)).thenReturn(response);

		SessionScheduleResponse result = sessionService.getSessionByIdForPublic(SESSION_ID);

		assertThat(result).isEqualTo(response);
	}

	@Test
	void getSessionByIdForPublic_Cancelled_ThrowsException() {
		Session session = createSession();
		session.setStatus(CinemaSessionStatus.CANCELLED);

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));

		assertThatThrownBy(() -> sessionService.getSessionByIdForPublic(SESSION_ID))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void updateSession_Success() {
		Session session = createSession();
		SessionUpdateRequest request = SessionUpdateRequest.builder().basePrice(new BigDecimal("300.00")).build();
		SessionAdminResponse response = createSessionAdminResponse();

		when(sessionRepository.findByIdWithLock(SESSION_ID)).thenReturn(Optional.of(session));
		when(sessionRepository.save(session)).thenReturn(session);
		when(sessionMapper.toSessionAdminResponse(session)).thenReturn(response);

		SessionAdminResponse result = sessionService.updateSession(SESSION_ID, request);

		assertThat(result).isEqualTo(response);
		verify(sessionMapper).updateSessionFromRequest(request, session);
	}

	@Test
	void deleteSession_Success() {
		when(sessionRepository.existsById(SESSION_ID)).thenReturn(true);

		sessionService.deleteSession(SESSION_ID);

		verify(sessionRepository).deleteById(SESSION_ID);
	}

	@Test
	void cancelSession_Success() {
		Session session = createSession();
		session.setStatus(CinemaSessionStatus.SCHEDULED);

		when(sessionRepository.findByIdWithLock(SESSION_ID)).thenReturn(Optional.of(session));
		when(sessionRepository.save(session)).thenReturn(session);

		sessionService.cancelSession(SESSION_ID);

		assertThat(session.getStatus()).isEqualTo(CinemaSessionStatus.CANCELLED);
		verify(sessionRepository).save(session);
	}

	@Test
	void reactivateSession_Success() {
		Session session = createSession();
		session.setStatus(CinemaSessionStatus.CANCELLED);

		when(sessionRepository.findByIdWithLock(SESSION_ID)).thenReturn(Optional.of(session));
		when(sessionRepository.existsConflictingSession(HALL_ID, START_TIME, START_TIME.plusMinutes(120), SESSION_ID))
				.thenReturn(false);
		when(sessionRepository.save(session)).thenReturn(session);

		sessionService.reactivateSession(SESSION_ID);

		assertThat(session.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
		verify(sessionRepository).save(session);
	}

	private Movie createMovie() {
		Movie movie = new Movie();
		movie.setId(MOVIE_ID);
		movie.setDurationMinutes(120);
		movie.setReleaseDate(LocalDate.now().minusDays(1));
		movie.setEndShowingDate(LocalDate.now().plusDays(30));
		return movie;
	}

	private CinemaHall createCinemaHall() {
		CinemaHall hall = new CinemaHall();
		hall.setId(HALL_ID);
		return hall;
	}

	private Session createSession() {
		Session session = new Session();
		session.setId(SESSION_ID);
		session.setStartTime(START_TIME);
		session.setBasePrice(BASE_PRICE);
		session.setMovie(createMovie());
		session.setHall(createCinemaHall());
		session.setStatus(CinemaSessionStatus.SCHEDULED);
		return session;
	}

	private SessionAdminResponse createSessionAdminResponse() {
		return SessionAdminResponse.builder().id(SESSION_ID).startTime(START_TIME).basePrice(BASE_PRICE)
				.status(CinemaSessionStatus.SCHEDULED).build();
	}

	private SessionScheduleResponse createSessionScheduleResponse() {
		return SessionScheduleResponse.builder().id(SESSION_ID).startTime(START_TIME).basePrice(BASE_PRICE)
				.status(CinemaSessionStatus.SCHEDULED).build();
	}
}