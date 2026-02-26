package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.projection.SessionAdminProjection;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionUpdateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
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

	private final Long SESSION_ID = 1L;
	private final Long MOVIE_ID = 2L;
	private final Long HALL_ID = 3L;
	private final LocalDateTime START_TIME = LocalDateTime.now().plusHours(2);

	@Test
	void createSession_Success() {
		SessionCreateRequest request = SessionCreateRequest.builder().startTime(START_TIME)
				.basePrice(BigDecimal.valueOf(250)).movieId(MOVIE_ID).hallId(HALL_ID).build();

		Movie movie = new Movie();
		movie.setId(MOVIE_ID);
		movie.setDurationMinutes(120);
		movie.setReleaseDate(LocalDate.now().minusDays(1));

		CinemaHall hall = new CinemaHall();
		hall.setId(HALL_ID);

		Session session = new Session();
		session.setId(SESSION_ID);

		SessionAdminProjection projection = new SessionAdminProjection() {
			@Override
			public Long getId() {
				return SESSION_ID;
			}

			@Override
			public LocalDateTime getStartTime() {
				return START_TIME;
			}

			@Override
			public LocalDateTime getEndTime() {
				return START_TIME.plusMinutes(120);
			}

			@Override
			public BigDecimal getBasePrice() {
				return BigDecimal.valueOf(250);
			}

			@Override
			public CinemaSessionStatus getStatus() {
				return CinemaSessionStatus.SCHEDULED;
			}

			@Override
			public Long getMovieId() {
				return MOVIE_ID;
			}

			@Override
			public String getMovieTitle() {
				return "Test Movie";
			}

			@Override
			public Integer getMovieDuration() {
				return 120;
			}

			@Override
			public Long getHallId() {
				return HALL_ID;
			}

			@Override
			public String getHallName() {
				return "Hall 1";
			}

			@Override
			public Integer getHallCapacity() {
				return 100;
			}

			@Override
			public Integer getTicketsSold() {
				return 0;
			}

			@Override
			public BigDecimal getTotalRevenue() {
				return BigDecimal.ZERO;
			}
		};

		SessionAdminResponse response = new SessionAdminResponse();
		response.setId(SESSION_ID);

		when(movieRepository.getReferenceById(MOVIE_ID)).thenReturn(movie);
		when(cinemaHallService.getHallEntityById(HALL_ID)).thenReturn(hall);
		when(sessionRepository.existsConflictingSession(HALL_ID, START_TIME, START_TIME.plusMinutes(120), null))
				.thenReturn(false);
		when(sessionMapper.toEntity(request)).thenReturn(session);
		when(sessionRepository.save(session)).thenReturn(session);
		when(sessionRepository.findAdminProjectionById(SESSION_ID)).thenReturn(Optional.of(projection));
		when(sessionMapper.toAdminResponse(projection)).thenReturn(response);

		SessionAdminResponse result = sessionService.createSession(request);

		assertThat(result).isEqualTo(response);
		verify(sessionRepository).save(session);
	}

	@Test
	void getSessionForPublic_Success() {
		Session session = new Session();
		session.setId(SESSION_ID);

		SessionScheduleResponse response = new SessionScheduleResponse();
		response.setId(SESSION_ID);

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
		when(sessionMapper.toScheduleResponse(session)).thenReturn(response);

		List<Object[]> seatData = List.<Object[]>of(new Object[] { SESSION_ID, 50 });
		when(sessionRepository.findAvailableSeatsBatch(List.of(SESSION_ID))).thenReturn(seatData);

		SessionScheduleResponse result = sessionService.getSessionForPublic(SESSION_ID);

		assertThat(result).isEqualTo(response);
	}

	@Test
	void getSessionForPublic_NotFound_ThrowsException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> sessionService.getSessionForPublic(SESSION_ID))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void updateSession_UpdatesStartTime_Success() {
		SessionUpdateRequest request = SessionUpdateRequest.builder().startTime(START_TIME.plusHours(1)).build();

		Movie movie = new Movie();
		movie.setDurationMinutes(120);

		CinemaHall hall = new CinemaHall();
		hall.setId(HALL_ID);

		Session session = new Session();
		session.setId(SESSION_ID);
		session.setHall(hall);
		session.setMovie(movie);
		session.setStartTime(START_TIME);

		SessionAdminProjection projection = new SessionAdminProjection() {
			@Override
			public Long getId() {
				return SESSION_ID;
			}

			@Override
			public LocalDateTime getStartTime() {
				return START_TIME.plusHours(1);
			}

			@Override
			public LocalDateTime getEndTime() {
				return START_TIME.plusHours(1).plusMinutes(120);
			}

			@Override
			public BigDecimal getBasePrice() {
				return BigDecimal.valueOf(250);
			}

			@Override
			public CinemaSessionStatus getStatus() {
				return CinemaSessionStatus.SCHEDULED;
			}

			@Override
			public Long getMovieId() {
				return MOVIE_ID;
			}

			@Override
			public String getMovieTitle() {
				return "Test Movie";
			}

			@Override
			public Integer getMovieDuration() {
				return 120;
			}

			@Override
			public Long getHallId() {
				return HALL_ID;
			}

			@Override
			public String getHallName() {
				return "Hall 1";
			}

			@Override
			public Integer getHallCapacity() {
				return 100;
			}

			@Override
			public Integer getTicketsSold() {
				return 0;
			}

			@Override
			public BigDecimal getTotalRevenue() {
				return BigDecimal.ZERO;
			}
		};

		SessionAdminResponse response = new SessionAdminResponse();
		response.setId(SESSION_ID);

		when(sessionRepository.findByIdWithLock(SESSION_ID)).thenReturn(Optional.of(session));
		when(sessionRepository.existsConflictingSession(HALL_ID, START_TIME.plusHours(1),
				START_TIME.plusHours(1).plusMinutes(120), SESSION_ID)).thenReturn(false);
		when(sessionRepository.save(session)).thenReturn(session);
		when(sessionRepository.findAdminProjectionById(SESSION_ID)).thenReturn(Optional.of(projection));
		when(sessionMapper.toAdminResponse(projection)).thenReturn(response);

		SessionAdminResponse result = sessionService.updateSession(SESSION_ID, request);

		assertThat(result).isEqualTo(response);
		assertThat(session.getStartTime()).isEqualTo(START_TIME.plusHours(1));
		verify(sessionRepository).save(session);
	}

	@Test
	void deleteSession_Success() {
		when(sessionRepository.existsById(SESSION_ID)).thenReturn(true);

		sessionService.deleteSession(SESSION_ID);

		verify(sessionRepository).deleteById(SESSION_ID);
	}

	@Test
	void cancelSession_Success() {
		Session session = new Session();
		session.setId(SESSION_ID);
		session.setStartTime(LocalDateTime.now().plusHours(2));
		session.setStatus(CinemaSessionStatus.SCHEDULED);

		when(sessionRepository.findByIdWithLock(SESSION_ID)).thenReturn(Optional.of(session));
		when(sessionRepository.save(session)).thenReturn(session);

		sessionService.cancelSession(SESSION_ID);

		assertThat(session.getStatus()).isEqualTo(CinemaSessionStatus.CANCELLED);
	}
}