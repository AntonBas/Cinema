package ua.lviv.bas.cinema.service.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.springframework.data.domain.Sort;

import ua.lviv.bas.cinema.domain.BookedSeat;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.filter.SessionFilter;
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
	private MovieRepository movieRepository;

	@Mock
	private CinemaHallService cinemaHallService;

	@InjectMocks
	private SessionService sessionService;

	private SessionCreateRequest sessionRequest;
	private Movie movie;
	private CinemaHall hall;
	private Session session;
	private SessionAdminResponse sessionAdminDto;
	private SessionScheduleResponse sessionScheduleDto;
	private Pageable pageable;

	@BeforeEach
	void setUp() {
		pageable = PageRequest.of(0, 20, Sort.by("startTime").ascending());

		movie = Movie.builder().id(1L).durationMinutes(120).title("Test Movie")
				.releaseDate(LocalDate.now().minusDays(1)).endShowingDate(LocalDate.now().plusDays(30)).build();

		hall = CinemaHall.builder().id(1L).name("Hall 1").seats(new ArrayList<>()).build();

		for (int i = 1; i <= 80; i++) {
			Seat seat = Seat.builder().id((long) i).row((i - 1) / 10 + 1).number((i - 1) % 10 + 1).active(true)
					.hall(hall).build();
			hall.getSeats().add(seat);
		}

		for (int i = 81; i <= 100; i++) {
			Seat seat = Seat.builder().id((long) i).row((i - 1) / 10 + 1).number((i - 1) % 10 + 1).active(false)
					.hall(hall).build();
			hall.getSeats().add(seat);
		}

		LocalDateTime startTime = LocalDateTime.now().plusHours(2);
		session = Session.builder().id(1L).startTime(startTime).basePrice(new BigDecimal("250.00")).movie(movie)
				.hall(hall).status(CinemaSessionStatus.SCHEDULED).bookedSeats(new ArrayList<>()).build();

		sessionRequest = SessionCreateRequest.builder().startTime(startTime).basePrice(new BigDecimal("250.00"))
				.movieId(1L).hallId(1L).build();

		sessionAdminDto = SessionAdminResponse.builder().id(1L).startTime(startTime).basePrice(new BigDecimal("250.00"))
				.status(CinemaSessionStatus.SCHEDULED).endTime(startTime.plusMinutes(120)).hallCapacity(80)
				.ticketsSold(0).totalRevenue(BigDecimal.ZERO).build();

		sessionScheduleDto = SessionScheduleResponse.builder().id(1L).startTime(startTime)
				.basePrice(new BigDecimal("250.00")).status(CinemaSessionStatus.SCHEDULED).availableSeats(80)
				.hallCapacity("80/80").endTime(startTime.plusMinutes(120)).build();
	}

	@Test
	void cancelSession_ShouldCancelSuccessfully() {
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(sessionRepository.save(any(Session.class))).thenReturn(session);

		sessionService.cancelSession(1L);

		assertThat(session.getStatus()).isEqualTo(CinemaSessionStatus.CANCELLED);
		verify(sessionRepository).save(session);
	}

	@Test
	void cancelSession_ShouldDoNothing_WhenAlreadyCancelled() {
		session.setStatus(CinemaSessionStatus.CANCELLED);
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

		sessionService.cancelSession(1L);

		verify(sessionRepository, times(0)).save(any(Session.class));
	}

	@Test
	void cancelSession_ShouldThrowException_WhenCannotCancelInactive() {
		session.setStatus(CinemaSessionStatus.COMPLETED);
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

		assertThatThrownBy(() -> sessionService.cancelSession(1L)).isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Cannot cancel inactive session");
	}

	@Test
	void cancelSession_ShouldThrowException_WhenLessThanHourBeforeStart() {
		session.setStartTime(LocalDateTime.now().plusMinutes(30));
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

		assertThatThrownBy(() -> sessionService.cancelSession(1L)).isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("less than 1 hour before start");
	}

	@Test
	void cancelSession_ShouldThrowException_WhenSessionNotFound() {
		when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> sessionService.cancelSession(99L)).isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void reactivateSession_ShouldReactivateSuccessfully() {
		session.setStatus(CinemaSessionStatus.CANCELLED);
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(sessionQueryService.findConflictingSessions(anyLong(), any(), any(), any()))
				.thenReturn(Collections.emptyList());
		when(sessionRepository.save(any(Session.class))).thenReturn(session);

		sessionService.reactivateSession(1L);

		assertThat(session.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
		verify(sessionRepository).save(session);
	}

	@Test
	void reactivateSession_ShouldThrowException_WhenNotCancelled() {
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

		assertThatThrownBy(() -> sessionService.reactivateSession(1L)).isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Only cancelled sessions can be reactivated");
	}

	@Test
	void reactivateSession_ShouldThrowException_WhenPastSession() {
		session.setStatus(CinemaSessionStatus.CANCELLED);
		session.setStartTime(LocalDateTime.now().minusHours(1));
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

		assertThatThrownBy(() -> sessionService.reactivateSession(1L)).isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Cannot reactivate past session");
	}

	@Test
	void reactivateSession_ShouldThrowException_WhenTimeConflict() {
		session.setStatus(CinemaSessionStatus.CANCELLED);
		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(sessionQueryService.findConflictingSessions(anyLong(), any(), any(), any()))
				.thenReturn(List.of(new Session()));

		assertThatThrownBy(() -> sessionService.reactivateSession(1L)).isInstanceOf(SessionTimeConflictException.class);
	}

	@Test
	void reactivateSession_ShouldThrowException_WhenSessionNotFound() {
		when(sessionRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> sessionService.reactivateSession(99L)).isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void createSession_ShouldCreateSuccessfully() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(cinemaHallService.getHallEntityById(1L)).thenReturn(hall);
		when(sessionQueryService.findConflictingSessions(anyLong(), any(), any(), any()))
				.thenReturn(Collections.emptyList());
		when(sessionMapper.toEntity(sessionRequest)).thenReturn(session);
		when(sessionRepository.save(session)).thenReturn(session);

		SessionAdminResponse adminResponseWithAllSeats = SessionAdminResponse.builder().id(1L)
				.startTime(session.getStartTime()).basePrice(new BigDecimal("250.00"))
				.status(CinemaSessionStatus.SCHEDULED).endTime(session.getStartTime().plusMinutes(120))
				.hallCapacity(100).ticketsSold(0).totalRevenue(BigDecimal.ZERO).build();
		when(sessionMapper.toAdminDto(session)).thenReturn(adminResponseWithAllSeats);

		SessionAdminResponse result = sessionService.createSession(sessionRequest);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getHallCapacity()).isEqualTo(100);
		verify(sessionRepository).save(session);
	}

	@Test
	void createSession_ShouldThrowException_WhenMovieNotFound() {
		when(movieRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> sessionService.createSession(sessionRequest))
				.isInstanceOf(MovieNotFoundException.class);
	}

	@Test
	void createSession_ShouldThrowException_WhenTimeConflict() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
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
		SessionAdminResponse adminResponseWithAllSeats = SessionAdminResponse.builder().id(1L)
				.startTime(session.getStartTime()).basePrice(new BigDecimal("250.00"))
				.status(CinemaSessionStatus.SCHEDULED).endTime(session.getStartTime().plusMinutes(120))
				.hallCapacity(100).ticketsSold(0).totalRevenue(BigDecimal.ZERO).build();

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(sessionMapper.toAdminDto(session)).thenReturn(adminResponseWithAllSeats);

		SessionAdminResponse result = sessionService.getSessionById(1L);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getHallCapacity()).isEqualTo(100);
		verify(sessionRepository).findById(1L);
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
		assertThat(result.getAvailableSeats()).isEqualTo(80);
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
	void updateSession_ShouldUpdateSuccessfully_WithoutStatus() {
		LocalDateTime pastTime = LocalDateTime.now().minusHours(2);
		Session pastSession = Session.builder().id(1L).startTime(pastTime).basePrice(new BigDecimal("250.00"))
				.movie(movie).hall(hall).status(CinemaSessionStatus.ONGOING).bookedSeats(new ArrayList<>()).build();

		SessionUpdateRequest updateRequest = SessionUpdateRequest.builder().basePrice(new BigDecimal("300.00")).build();

		SessionAdminResponse adminDto = SessionAdminResponse.builder().id(1L).startTime(pastTime)
				.basePrice(new BigDecimal("300.00")).status(CinemaSessionStatus.ONGOING)
				.endTime(pastTime.plusMinutes(120)).hallCapacity(80).ticketsSold(0).totalRevenue(BigDecimal.ZERO)
				.build();

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(pastSession));
		when(sessionRepository.save(any(Session.class))).thenReturn(pastSession);
		when(sessionMapper.toAdminDto(pastSession)).thenReturn(adminDto);

		SessionAdminResponse result = sessionService.updateSession(1L, updateRequest);

		assertThat(result).isNotNull();
		assertThat(result.getBasePrice()).isEqualByComparingTo(new BigDecimal("300.00"));
		verify(sessionMapper).updateEntityFromDto(updateRequest, pastSession);
	}

	@Test
	void updateSession_ShouldUpdateSuccessfully_WhenOnlyChangingStartTime() {
		SessionUpdateRequest updateRequest = SessionUpdateRequest.builder().startTime(LocalDateTime.now().plusHours(5))
				.build();

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(sessionQueryService.findConflictingSessions(anyLong(), any(), any(), any()))
				.thenReturn(Collections.emptyList());
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
	void updateSession_ShouldThrowException_WhenMovieNotFound() {
		SessionUpdateRequest updateRequest = SessionUpdateRequest.builder().movieId(99L).build();

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(movieRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> sessionService.updateSession(1L, updateRequest))
				.isInstanceOf(MovieNotFoundException.class);
	}

	@Test
	void updateSession_ShouldChangeMovie_WhenMovieIdProvided() {
		Movie newMovie = Movie.builder().id(2L).durationMinutes(150).title("New Movie")
				.releaseDate(LocalDate.now().minusDays(1)).build();

		SessionUpdateRequest updateRequest = SessionUpdateRequest.builder().movieId(2L).build();

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(movieRepository.findById(2L)).thenReturn(Optional.of(newMovie));
		when(sessionRepository.save(any(Session.class))).thenReturn(session);
		when(sessionMapper.toAdminDto(session)).thenReturn(sessionAdminDto);

		SessionAdminResponse result = sessionService.updateSession(1L, updateRequest);

		assertThat(result).isNotNull();
		verify(sessionRepository).save(session);
		assertThat(session.getMovie().getId()).isEqualTo(2L);
	}

	@Test
	void updateSession_ShouldChangeHall_WhenHallIdProvided() {
		CinemaHall newHall = CinemaHall.builder().id(2L).name("Hall 2").build();

		SessionUpdateRequest updateRequest = SessionUpdateRequest.builder().hallId(2L).build();

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(cinemaHallService.getHallEntityById(2L)).thenReturn(newHall);
		when(sessionRepository.save(any(Session.class))).thenReturn(session);
		when(sessionMapper.toAdminDto(session)).thenReturn(sessionAdminDto);

		SessionAdminResponse result = sessionService.updateSession(1L, updateRequest);

		assertThat(result).isNotNull();
		verify(sessionRepository).save(session);
		assertThat(session.getHall().getId()).isEqualTo(2L);
	}

	@Test
	void updateSession_ShouldNotChangeMovie_WhenSameMovieId() {
		SessionUpdateRequest updateRequest = SessionUpdateRequest.builder().movieId(1L).build();

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(sessionRepository.save(any(Session.class))).thenReturn(session);
		when(sessionMapper.toAdminDto(session)).thenReturn(sessionAdminDto);

		SessionAdminResponse result = sessionService.updateSession(1L, updateRequest);

		assertThat(result).isNotNull();
		verify(movieRepository, times(0)).findById(anyLong());
	}

	@Test
	void updateSession_ShouldNotChangeHall_WhenSameHallId() {
		SessionUpdateRequest updateRequest = SessionUpdateRequest.builder().hallId(1L).build();

		when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
		when(sessionRepository.save(any(Session.class))).thenReturn(session);
		when(sessionMapper.toAdminDto(session)).thenReturn(sessionAdminDto);

		SessionAdminResponse result = sessionService.updateSession(1L, updateRequest);

		assertThat(result).isNotNull();
		verify(cinemaHallService, times(0)).getHallEntityById(anyLong());
	}

	@Test
	void updateSession_ShouldValidateMovieAvailability_WhenStartTimeChanged() {
		Movie movieWithFutureRelease = Movie.builder().id(1L).durationMinutes(120)
				.releaseDate(LocalDate.now().plusDays(5)).build();

		Session sessionWithFutureMovie = Session.builder().id(2L).startTime(LocalDateTime.now().plusHours(2))
				.movie(movieWithFutureRelease).hall(hall).status(CinemaSessionStatus.SCHEDULED).build();

		SessionUpdateRequest updateRequest = SessionUpdateRequest.builder().startTime(LocalDateTime.now().plusDays(1))
				.build();

		when(sessionRepository.findById(2L)).thenReturn(Optional.of(sessionWithFutureMovie));

		assertThatThrownBy(() -> sessionService.updateSession(2L, updateRequest))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("releases on");
	}

	@Test
	void deleteSession_ShouldDeleteSuccessfully() {
		when(sessionRepository.existsById(1L)).thenReturn(true);
		doNothing().when(sessionRepository).deleteById(1L);

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

		when(sessionQueryService.findByStartTimeBetween(any(), any(), any(), eq(true))).thenReturn(sessionPage);
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

		when(sessionQueryService.findByStartTimeBetween(any(), any(), any(), eq(false))).thenReturn(sessionPage);
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

		when(sessionQueryService.findByStartTimeBetween(any(), any(), any(), eq(false))).thenReturn(sessionPage);
		when(sessionMapper.toScheduleDto(session)).thenReturn(sessionScheduleDto);

		Page<SessionScheduleResponse> result = sessionService.getUpcomingScheduleSessions(7, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void hasTimeConflict_ShouldReturnTrue_WhenConflictExists() {
		LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 15, 18, 0);
		Session conflictingSession = Session.builder().id(2L).build();

		when(sessionQueryService.findConflictingSessions(1L, fixedTime, fixedTime.plusMinutes(120), null))
				.thenReturn(List.of(conflictingSession));

		boolean result = sessionService.hasTimeConflict(1L, fixedTime, 120, null);

		assertThat(result).isTrue();
	}

	@Test
	void hasTimeConflict_ShouldReturnFalse_WhenNoConflict() {
		LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 15, 18, 0);

		when(sessionQueryService.findConflictingSessions(1L, fixedTime, fixedTime.plusMinutes(120), null))
				.thenReturn(Collections.emptyList());

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
	void toAdminResponse_ShouldMapWithCalculatedValues() {
		when(sessionMapper.toAdminDto(session)).thenReturn(sessionAdminDto);

		SessionAdminResponse result = sessionService.toAdminResponse(session);

		assertThat(result).isNotNull();
		assertThat(result.getEndTime()).isEqualTo(session.getStartTime().plusMinutes(120));
		assertThat(result.getHallCapacity()).isEqualTo(100);
		assertThat(result.getTicketsSold()).isEqualTo(0);
		assertThat(result.getTotalRevenue()).isEqualTo(BigDecimal.ZERO);
	}

	@Test
	void toAdminResponse_ShouldCalculateRevenue_WhenTicketsSold() {
		Session sessionWithTickets = Session.builder().id(2L).startTime(LocalDateTime.now().plusHours(2))
				.basePrice(new BigDecimal("300.00")).movie(movie).hall(hall).status(CinemaSessionStatus.SCHEDULED)
				.bookedSeats(createBookedSeats(5)).build();

		SessionAdminResponse adminDto = SessionAdminResponse.builder().id(2L)
				.startTime(sessionWithTickets.getStartTime()).basePrice(new BigDecimal("300.00"))
				.status(CinemaSessionStatus.SCHEDULED).build();

		when(sessionMapper.toAdminDto(sessionWithTickets)).thenReturn(adminDto);

		SessionAdminResponse result = sessionService.toAdminResponse(sessionWithTickets);

		assertThat(result).isNotNull();
		assertThat(result.getTicketsSold()).isEqualTo(5);
		assertThat(result.getTotalRevenue()).isEqualByComparingTo(new BigDecimal("1500.00"));
	}

	@Test
	void toScheduleResponse_ShouldMapWithCalculatedValues() {
		when(sessionMapper.toScheduleDto(session)).thenReturn(sessionScheduleDto);

		SessionScheduleResponse result = sessionService.toScheduleResponse(session);

		assertThat(result).isNotNull();
		assertThat(result.getEndTime()).isEqualTo(session.getStartTime().plusMinutes(120));
		assertThat(result.getAvailableSeats()).isEqualTo(80);
	}

	@Test
	void toScheduleResponse_ShouldCalculateAvailableSeats_WhenSomeSeatsBooked() {
		Session sessionWithBookedSeats = Session.builder().id(3L).startTime(LocalDateTime.now().plusHours(2))
				.movie(movie).hall(hall).status(CinemaSessionStatus.SCHEDULED).bookedSeats(createBookedSeats(10))
				.build();

		SessionScheduleResponse scheduleDto = SessionScheduleResponse.builder().id(3L)
				.startTime(sessionWithBookedSeats.getStartTime()).status(CinemaSessionStatus.SCHEDULED).build();

		when(sessionMapper.toScheduleDto(sessionWithBookedSeats)).thenReturn(scheduleDto);

		SessionScheduleResponse result = sessionService.toScheduleResponse(sessionWithBookedSeats);

		assertThat(result).isNotNull();
		assertThat(result.getAvailableSeats()).isEqualTo(70);
		assertThat(result.getHallCapacity()).isEqualTo("70/80");
	}

	private List<BookedSeat> createBookedSeats(int count) {
		List<BookedSeat> bookedSeats = new ArrayList<>();
		for (int i = 1; i <= count; i++) {
			Ticket ticket = Ticket.builder().id((long) i).build();
			BookedSeat bookedSeat = BookedSeat.builder().id((long) i).ticket(ticket).build();
			bookedSeats.add(bookedSeat);
		}
		return bookedSeats;
	}
}