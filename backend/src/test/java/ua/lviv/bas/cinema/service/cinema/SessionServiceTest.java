package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionUpdateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionOperationException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionTimeConflictException;
import ua.lviv.bas.cinema.mapper.cinema.SessionMapper;
import ua.lviv.bas.cinema.repository.cinema.MovieRepository;
import ua.lviv.bas.cinema.repository.cinema.SessionRepository;
import ua.lviv.bas.cinema.repository.cinema.projection.SessionAdminProjection;

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

	private Session session;
	private Movie movie;
	private CinemaHall hall;
	private SessionAdminResponse adminResponse;
	private SessionAdminProjection adminProjection;

	private static final Long SESSION_ID = 1L;
	private static final Long MOVIE_ID = 2L;
	private static final Long HALL_ID = 3L;
	private static final BigDecimal BASE_PRICE = new BigDecimal("250.00");
	private static final String MOVIE_TITLE = "Test Movie";
	private static final String HALL_NAME = "Hall 1";

	@BeforeEach
	void setUp() {
		movie = Movie.builder().id(MOVIE_ID).title(MOVIE_TITLE).durationMinutes(120)
				.releaseDate(LocalDate.now().minusDays(1)).build();

		hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).seats(new ArrayList<>()).build();

		session = Session.builder().id(SESSION_ID).movie(movie).hall(hall).startTime(LocalDateTime.now().plusHours(2))
				.basePrice(BASE_PRICE).status(CinemaSessionStatus.SCHEDULED).build();

		adminResponse = new SessionAdminResponse(SESSION_ID, session.getStartTime(), null, BASE_PRICE,
				CinemaSessionStatus.SCHEDULED, MOVIE_ID, MOVIE_TITLE, 120, HALL_ID, HALL_NAME, 100, 0, BigDecimal.ZERO);

		LocalDateTime startTime = session.getStartTime();
		LocalDateTime endTime = startTime.plusMinutes(120);

		adminProjection = new SessionAdminProjection(SESSION_ID, Timestamp.valueOf(startTime),
				Timestamp.valueOf(endTime), BASE_PRICE, CinemaSessionStatus.SCHEDULED.name(), MOVIE_ID, MOVIE_TITLE,
				120, HALL_ID, HALL_NAME, 100L, 0L, BigDecimal.ZERO);
	}

	@Test
	void createSession_Success() {
		LocalDateTime startTime = LocalDateTime.now().plusHours(2);
		SessionCreateRequest request = new SessionCreateRequest(startTime, BASE_PRICE, MOVIE_ID, HALL_ID);

		when(movieRepository.getReferenceById(MOVIE_ID)).thenReturn(movie);
		when(cinemaHallService.getHallEntityById(HALL_ID)).thenReturn(hall);
		when(sessionRepository.existsConflictingSession(HALL_ID, startTime, startTime.plusMinutes(120), null))
				.thenReturn(false);
		when(sessionMapper.toEntity(request)).thenReturn(session);
		when(sessionRepository.save(session)).thenReturn(session);
		when(sessionRepository.findAdminProjectionById(SESSION_ID)).thenReturn(Optional.of(adminProjection));
		when(sessionMapper.toAdminResponse(adminProjection)).thenReturn(adminResponse);

		SessionAdminResponse result = sessionService.createSession(request);

		assertThat(result).isEqualTo(adminResponse);
		verify(sessionRepository).save(session);
	}

	@Test
	void createSession_WhenTimeConflict_ThrowsException() {
		LocalDateTime startTime = LocalDateTime.now().plusHours(2);
		SessionCreateRequest request = new SessionCreateRequest(startTime, BASE_PRICE, MOVIE_ID, HALL_ID);

		when(movieRepository.getReferenceById(MOVIE_ID)).thenReturn(movie);
		when(cinemaHallService.getHallEntityById(HALL_ID)).thenReturn(hall);
		when(sessionRepository.existsConflictingSession(HALL_ID, startTime, startTime.plusMinutes(120), null))
				.thenReturn(true);

		assertThatThrownBy(() -> sessionService.createSession(request))
				.isInstanceOf(SessionTimeConflictException.class);

		verify(sessionRepository, never()).save(any());
	}

	@Test
	void getSessionForAdmin_Success() {
		when(sessionRepository.findAdminProjectionById(SESSION_ID)).thenReturn(Optional.of(adminProjection));
		when(sessionMapper.toAdminResponse(adminProjection)).thenReturn(adminResponse);

		SessionAdminResponse result = sessionService.getSessionForAdmin(SESSION_ID);

		assertThat(result).isEqualTo(adminResponse);
	}

	@Test
	void getSessionForAdmin_WhenNotFound_ThrowsException() {
		when(sessionRepository.findAdminProjectionById(SESSION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> sessionService.getSessionForAdmin(SESSION_ID))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void updateSession_WhenStartTimeChanged_Success() {
		LocalDateTime newStartTime = LocalDateTime.now().plusHours(3);
		SessionUpdateRequest request = new SessionUpdateRequest(newStartTime, null, null, null);

		when(sessionRepository.findByIdWithLock(SESSION_ID)).thenReturn(Optional.of(session));
		when(sessionRepository.existsConflictingSession(HALL_ID, newStartTime,
				newStartTime.plusMinutes(movie.getDurationMinutes()), SESSION_ID)).thenReturn(false);
		when(sessionRepository.save(session)).thenReturn(session);
		when(sessionRepository.findAdminProjectionById(SESSION_ID)).thenReturn(Optional.of(adminProjection));
		when(sessionMapper.toAdminResponse(adminProjection)).thenReturn(adminResponse);

		SessionAdminResponse result = sessionService.updateSession(SESSION_ID, request);

		assertThat(result).isEqualTo(adminResponse);
		assertThat(session.getStartTime()).isEqualTo(newStartTime);
		verify(sessionRepository).save(session);
	}

	@Test
	void updateSession_WhenNoChanges_DoesNotSave() {
		SessionUpdateRequest request = new SessionUpdateRequest(null, null, null, null);

		when(sessionRepository.findByIdWithLock(SESSION_ID)).thenReturn(Optional.of(session));
		when(sessionRepository.findAdminProjectionById(SESSION_ID)).thenReturn(Optional.of(adminProjection));
		when(sessionMapper.toAdminResponse(adminProjection)).thenReturn(adminResponse);

		SessionAdminResponse result = sessionService.updateSession(SESSION_ID, request);

		assertThat(result).isEqualTo(adminResponse);
		verify(sessionRepository, never()).save(any());
	}

	@Test
	void updateSession_WhenNotFound_ThrowsException() {
		when(sessionRepository.findByIdWithLock(SESSION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(
				() -> sessionService.updateSession(SESSION_ID, new SessionUpdateRequest(null, null, null, null)))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void deleteSession_Success() {
		when(sessionRepository.existsById(SESSION_ID)).thenReturn(true);

		sessionService.deleteSession(SESSION_ID);

		verify(sessionRepository).deleteById(SESSION_ID);
	}

	@Test
	void deleteSession_WhenNotFound_ThrowsException() {
		when(sessionRepository.existsById(SESSION_ID)).thenReturn(false);

		assertThatThrownBy(() -> sessionService.deleteSession(SESSION_ID)).isInstanceOf(SessionNotFoundException.class);

		verify(sessionRepository, never()).deleteById(any());
	}

	@Test
	void cancelSession_Success() {
		when(sessionRepository.findByIdWithLock(SESSION_ID)).thenReturn(Optional.of(session));
		when(sessionRepository.save(session)).thenReturn(session);

		sessionService.cancelSession(SESSION_ID);

		assertThat(session.getStatus()).isEqualTo(CinemaSessionStatus.CANCELLED);
		verify(sessionRepository).save(session);
	}

	@Test
	void cancelSession_WhenAlreadyCancelled_DoesNothing() {
		session.setStatus(CinemaSessionStatus.CANCELLED);
		when(sessionRepository.findByIdWithLock(SESSION_ID)).thenReturn(Optional.of(session));

		sessionService.cancelSession(SESSION_ID);

		verify(sessionRepository, never()).save(any());
	}

	@Test
	void cancelSession_WhenTooLate_ThrowsException() {
		session.setStartTime(LocalDateTime.now().plusMinutes(30));
		when(sessionRepository.findByIdWithLock(SESSION_ID)).thenReturn(Optional.of(session));

		assertThatThrownBy(() -> sessionService.cancelSession(SESSION_ID))
				.isInstanceOf(SessionOperationException.class);

		verify(sessionRepository, never()).save(any());
	}

	@Test
	void cancelSession_WhenCompleted_ThrowsException() {
		session.setStatus(CinemaSessionStatus.COMPLETED);
		when(sessionRepository.findByIdWithLock(SESSION_ID)).thenReturn(Optional.of(session));

		assertThatThrownBy(() -> sessionService.cancelSession(SESSION_ID))
				.isInstanceOf(SessionOperationException.class);
	}

	@Test
	void reactivateSession_Success() {
		session.setStatus(CinemaSessionStatus.CANCELLED);

		when(sessionRepository.findByIdWithLock(SESSION_ID)).thenReturn(Optional.of(session));
		when(sessionRepository.existsConflictingSession(HALL_ID, session.getStartTime(),
				session.getStartTime().plusMinutes(movie.getDurationMinutes()), SESSION_ID)).thenReturn(false);
		when(sessionRepository.save(session)).thenReturn(session);

		sessionService.reactivateSession(SESSION_ID);

		assertThat(session.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
		verify(sessionRepository).save(session);
	}

	@Test
	void reactivateSession_WhenNotCancelled_ThrowsException() {
		when(sessionRepository.findByIdWithLock(SESSION_ID)).thenReturn(Optional.of(session));

		assertThatThrownBy(() -> sessionService.reactivateSession(SESSION_ID))
				.isInstanceOf(SessionOperationException.class);
	}

	@Test
	void reactivateSession_WhenTimeConflict_ThrowsException() {
		session.setStatus(CinemaSessionStatus.CANCELLED);

		when(sessionRepository.findByIdWithLock(SESSION_ID)).thenReturn(Optional.of(session));
		when(sessionRepository.existsConflictingSession(HALL_ID, session.getStartTime(),
				session.getStartTime().plusMinutes(movie.getDurationMinutes()), SESSION_ID)).thenReturn(true);

		assertThatThrownBy(() -> sessionService.reactivateSession(SESSION_ID))
				.isInstanceOf(SessionTimeConflictException.class);

		verify(sessionRepository, never()).save(any());
	}
}