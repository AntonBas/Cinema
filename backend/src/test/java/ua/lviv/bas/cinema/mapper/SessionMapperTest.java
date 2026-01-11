package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionUpdateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;

class SessionMapperTest {

	private SessionMapper sessionMapper;
	private LocalDateTime futureTime;
	private Movie movie;
	private CinemaHall hall;
	private Session session;

	@BeforeEach
	void setUp() {
		sessionMapper = Mappers.getMapper(SessionMapper.class);
		futureTime = LocalDateTime.now().plusHours(2);

		movie = Movie.builder().id(1L).title("Test Movie").durationMinutes(120).posterFileName("poster.jpg")
				.ageRating(AgeRating.PEGI_12).build();

		List<Seat> seats = new ArrayList<>();
		for (int i = 1; i <= 100; i++) {
			Seat seat = Seat.builder().id((long) i).row((i - 1) / 10 + 1).number((i - 1) % 10 + 1)
					.seatType(i <= 80 ? SeatType.STANDARD : SeatType.VIP).build();
			seats.add(seat);
		}

		hall = CinemaHall.builder().id(1L).name("Hall 1").seats(seats).build();

		session = Session.builder().id(1L).startTime(futureTime).basePrice(new BigDecimal("250.00")).movie(movie)
				.hall(hall).status(CinemaSessionStatus.SCHEDULED).bookings(new ArrayList<>())
				.bookedSeats(new ArrayList<>()).build();
	}

	@Test
	void toSessionAdminResponse_ShouldMapAllFields() {
		SessionAdminResponse result = sessionMapper.toSessionAdminResponse(session);

		assertAll(() -> assertThat(result).isNotNull(), () -> assertThat(result.getId()).isEqualTo(1L),
				() -> assertThat(result.getStartTime()).isEqualTo(futureTime),
				() -> assertThat(result.getBasePrice()).isEqualByComparingTo("250.00"),
				() -> assertThat(result.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED),
				() -> assertThat(result.getMovieId()).isEqualTo(1L),
				() -> assertThat(result.getMovieTitle()).isEqualTo("Test Movie"),
				() -> assertThat(result.getMovieDuration()).isEqualTo(120),
				() -> assertThat(result.getHallId()).isEqualTo(1L),
				() -> assertThat(result.getHallName()).isEqualTo("Hall 1"),
				() -> assertThat(result.getEndTime()).isNull(), () -> assertThat(result.getHallCapacity()).isNull(),
				() -> assertThat(result.getTicketsSold()).isNull(),
				() -> assertThat(result.getTotalRevenue()).isNull());
	}

	@ParameterizedTest
	@EnumSource(CinemaSessionStatus.class)
	void toSessionAdminResponse_ShouldMapAllStatusValues(CinemaSessionStatus status) {
		session.setStatus(status);
		SessionAdminResponse result = sessionMapper.toSessionAdminResponse(session);
		assertThat(result.getStatus()).isEqualTo(status);
	}

	@Test
	void toSessionAdminResponse_ShouldHandleNullEntity() {
		SessionAdminResponse result = sessionMapper.toSessionAdminResponse(null);
		assertThat(result).isNull();
	}

	@Test
	void toSessionAdminResponse_ShouldHandleNullMovie() {
		session.setMovie(null);
		SessionAdminResponse result = sessionMapper.toSessionAdminResponse(session);

		assertAll(() -> assertThat(result.getMovieId()).isNull(), () -> assertThat(result.getMovieTitle()).isNull(),
				() -> assertThat(result.getMovieDuration()).isNull());
	}

	@Test
	void toSessionAdminResponse_ShouldHandleNullHall() {
		session.setHall(null);
		SessionAdminResponse result = sessionMapper.toSessionAdminResponse(session);

		assertAll(() -> assertThat(result.getHallId()).isNull(), () -> assertThat(result.getHallName()).isNull());
	}

	@Test
	void toSessionAdminResponse_ShouldHandleNullBasePrice() {
		session.setBasePrice(null);
		SessionAdminResponse result = sessionMapper.toSessionAdminResponse(session);
		assertThat(result.getBasePrice()).isNull();
	}

	@Test
	void toSessionAdminResponse_ShouldHandleNullStartTime() {
		session.setStartTime(null);
		SessionAdminResponse result = sessionMapper.toSessionAdminResponse(session);
		assertThat(result.getStartTime()).isNull();
	}

	@Test
	void toSessionAdminResponse_ShouldHandleNullStatus() {
		session.setStatus(null);
		SessionAdminResponse result = sessionMapper.toSessionAdminResponse(session);
		assertThat(result.getStatus()).isNull();
	}

	@Test
	void toSessionScheduleResponse_ShouldMapAllFields() {
		SessionScheduleResponse result = sessionMapper.toSessionScheduleResponse(session);

		assertAll(() -> assertThat(result).isNotNull(), () -> assertThat(result.getId()).isEqualTo(1L),
				() -> assertThat(result.getStartTime()).isEqualTo(futureTime),
				() -> assertThat(result.getBasePrice()).isEqualByComparingTo("250.00"),
				() -> assertThat(result.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED),
				() -> assertThat(result.getMovieId()).isEqualTo(1L),
				() -> assertThat(result.getMovieTitle()).isEqualTo("Test Movie"),
				() -> assertThat(result.getMoviePosterFileName()).isEqualTo("poster.jpg"),
				() -> assertThat(result.getMovieAgeRating()).isEqualTo("PEGI_12"),
				() -> assertThat(result.getMovieDuration()).isEqualTo(120),
				() -> assertThat(result.getHallId()).isEqualTo(1L),
				() -> assertThat(result.getHallName()).isEqualTo("Hall 1"),
				() -> assertThat(result.getEndTime()).isNull(), () -> assertThat(result.getAvailableSeats()).isNull(),
				() -> assertThat(result.getHallCapacity()).isNull());
	}

	@ParameterizedTest
	@EnumSource(CinemaSessionStatus.class)
	void toSessionScheduleResponse_ShouldMapAllStatusValues(CinemaSessionStatus status) {
		session.setStatus(status);
		SessionScheduleResponse result = sessionMapper.toSessionScheduleResponse(session);
		assertThat(result.getStatus()).isEqualTo(status);
	}

	@Test
	void toSessionScheduleResponse_ShouldHandleNullEntity() {
		SessionScheduleResponse result = sessionMapper.toSessionScheduleResponse(null);
		assertThat(result).isNull();
	}

	@Test
	void toSessionScheduleResponse_ShouldHandleNullMovie() {
		session.setMovie(null);
		SessionScheduleResponse result = sessionMapper.toSessionScheduleResponse(session);

		assertAll(() -> assertThat(result.getMovieId()).isNull(), () -> assertThat(result.getMovieTitle()).isNull(),
				() -> assertThat(result.getMoviePosterFileName()).isNull(),
				() -> assertThat(result.getMovieAgeRating()).isNull(),
				() -> assertThat(result.getMovieDuration()).isNull());
	}

	@Test
	void toSessionScheduleResponse_ShouldHandleMovieWithoutPoster() {
		movie.setPosterFileName(null);
		SessionScheduleResponse result = sessionMapper.toSessionScheduleResponse(session);
		assertThat(result.getMoviePosterFileName()).isNull();
	}

	@Test
	void toSessionScheduleResponse_ShouldHandleMovieWithoutAgeRating() {
		movie.setAgeRating(null);
		SessionScheduleResponse result = sessionMapper.toSessionScheduleResponse(session);
		assertThat(result.getMovieAgeRating()).isNull();
	}

	@ParameterizedTest
	@ValueSource(ints = { 60, 90, 120, 150, 180 })
	void toSessionScheduleResponse_ShouldMapDifferentMovieDurations(int duration) {
		movie.setDurationMinutes(duration);
		SessionScheduleResponse result = sessionMapper.toSessionScheduleResponse(session);
		assertThat(result.getMovieDuration()).isEqualTo(duration);
	}

	@Test
	void toSessionScheduleResponse_ShouldHandleNullHall() {
		session.setHall(null);
		SessionScheduleResponse result = sessionMapper.toSessionScheduleResponse(session);

		assertAll(() -> assertThat(result.getHallId()).isNull(), () -> assertThat(result.getHallName()).isNull());
	}

	@Test
	void toSessionScheduleResponse_ShouldHandleNullBasePrice() {
		session.setBasePrice(null);
		SessionScheduleResponse result = sessionMapper.toSessionScheduleResponse(session);
		assertThat(result.getBasePrice()).isNull();
	}

	@Test
	void toSessionScheduleResponse_ShouldHandleNullStartTime() {
		session.setStartTime(null);
		SessionScheduleResponse result = sessionMapper.toSessionScheduleResponse(session);
		assertThat(result.getStartTime()).isNull();
	}

	@Test
	void toSessionScheduleResponse_ShouldHandleNullStatus() {
		session.setStatus(null);
		SessionScheduleResponse result = sessionMapper.toSessionScheduleResponse(session);
		assertThat(result.getStatus()).isNull();
	}

	@Test
	void toSession_ShouldMapRequestToEntityWithDefaultValues() {
		SessionCreateRequest request = SessionCreateRequest.builder().startTime(futureTime)
				.basePrice(new BigDecimal("300.00")).movieId(2L).hallId(3L).build();

		Session result = sessionMapper.toSession(request);

		assertAll(() -> assertThat(result).isNotNull(), () -> assertThat(result.getId()).isNull(),
				() -> assertThat(result.getStartTime()).isEqualTo(request.getStartTime()),
				() -> assertThat(result.getBasePrice()).isEqualByComparingTo("300.00"),
				() -> assertThat(result.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED),
				() -> assertThat(result.getMovie()).isNull(), () -> assertThat(result.getHall()).isNull(),
				() -> assertThat(result.getBookings()).isNotNull(), () -> assertThat(result.getBookings()).isEmpty(),
				() -> assertThat(result.getBookedSeats()).isNotNull(),
				() -> assertThat(result.getBookedSeats()).isEmpty());
	}

	@Test
	void toSession_ShouldHandleNullRequest() {
		Session result = sessionMapper.toSession(null);
		assertThat(result).isNull();
	}

	@Test
	void toSession_ShouldHandleNullBasePrice() {
		SessionCreateRequest request = SessionCreateRequest.builder().startTime(futureTime).basePrice(null).movieId(1L)
				.hallId(1L).build();

		Session result = sessionMapper.toSession(request);
		assertThat(result.getBasePrice()).isNull();
	}

	@Test
	void toSession_ShouldHandleNullStartTime() {
		SessionCreateRequest request = SessionCreateRequest.builder().startTime(null)
				.basePrice(new BigDecimal("200.00")).movieId(1L).hallId(1L).build();

		Session result = sessionMapper.toSession(request);
		assertThat(result.getStartTime()).isNull();
	}

	@Test
	void toSession_ShouldHandleNullIds() {
		SessionCreateRequest request = SessionCreateRequest.builder().startTime(futureTime)
				.basePrice(new BigDecimal("200.00")).movieId(null).hallId(null).build();

		Session result = sessionMapper.toSession(request);

		assertAll(() -> assertThat(result.getStartTime()).isEqualTo(futureTime),
				() -> assertThat(result.getBasePrice()).isEqualByComparingTo("200.00"),
				() -> assertThat(result.getMovie()).isNull(), () -> assertThat(result.getHall()).isNull());
	}

	@Test
	void toSession_ShouldMapZeroBasePrice() {
		SessionCreateRequest request = SessionCreateRequest.builder().startTime(futureTime).basePrice(BigDecimal.ZERO)
				.movieId(1L).hallId(1L).build();

		Session result = sessionMapper.toSession(request);
		assertThat(result.getBasePrice()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void updateSessionFromRequest_ShouldUpdateNonNullFields() {
		SessionUpdateRequest updateRequest = SessionUpdateRequest.builder().startTime(futureTime.plusHours(1))
				.basePrice(new BigDecimal("350.00")).build();

		Session existingSession = Session.builder().id(1L).startTime(futureTime).basePrice(new BigDecimal("250.00"))
				.status(CinemaSessionStatus.SCHEDULED).bookings(new ArrayList<>()).bookedSeats(new ArrayList<>())
				.build();

		sessionMapper.updateSessionFromRequest(updateRequest, existingSession);

		assertAll(() -> assertThat(existingSession.getId()).isEqualTo(1L),
				() -> assertThat(existingSession.getStartTime()).isEqualTo(futureTime.plusHours(1)),
				() -> assertThat(existingSession.getBasePrice()).isEqualByComparingTo("350.00"),
				() -> assertThat(existingSession.getMovie()).isNull(),
				() -> assertThat(existingSession.getHall()).isNull());
	}

	@Test
	void updateSessionFromRequest_ShouldNotUpdateStatus() {
		SessionUpdateRequest updateRequest = SessionUpdateRequest.builder().build();

		Session existingSession = Session.builder().id(1L).startTime(futureTime).basePrice(new BigDecimal("250.00"))
				.status(CinemaSessionStatus.SCHEDULED).build();

		sessionMapper.updateSessionFromRequest(updateRequest, existingSession);

		assertThat(existingSession.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
	}

	@Test
	void updateSessionFromRequest_ShouldIgnoreNullFields() {
		SessionUpdateRequest updateRequest = SessionUpdateRequest.builder().basePrice(new BigDecimal("400.00"))
				.startTime(null).build();

		Session existingSession = Session.builder().id(1L).startTime(futureTime).basePrice(new BigDecimal("250.00"))
				.status(CinemaSessionStatus.SCHEDULED).build();

		sessionMapper.updateSessionFromRequest(updateRequest, existingSession);

		assertAll(() -> assertThat(existingSession.getStartTime()).isEqualTo(futureTime),
				() -> assertThat(existingSession.getBasePrice()).isEqualByComparingTo("400.00"),
				() -> assertThat(existingSession.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED));
	}

	@Test
	void updateSessionFromRequest_ShouldHandleNullRequest() {
		Session existingSession = Session.builder().id(1L).startTime(futureTime).basePrice(new BigDecimal("250.00"))
				.build();

		sessionMapper.updateSessionFromRequest(null, existingSession);

		assertAll(() -> assertThat(existingSession.getStartTime()).isEqualTo(futureTime),
				() -> assertThat(existingSession.getBasePrice()).isEqualByComparingTo("250.00"));
	}

	@Test
	void updateSessionFromRequest_ShouldPreserveIdAndRelations() {
		SessionUpdateRequest updateRequest = SessionUpdateRequest.builder().basePrice(new BigDecimal("500.00")).build();

		Session existingSession = Session.builder().id(99L).movie(movie).hall(hall).bookings(new ArrayList<>())
				.bookedSeats(new ArrayList<>()).build();

		sessionMapper.updateSessionFromRequest(updateRequest, existingSession);

		assertAll(() -> assertThat(existingSession.getId()).isEqualTo(99L),
				() -> assertThat(existingSession.getMovie()).isEqualTo(movie),
				() -> assertThat(existingSession.getHall()).isEqualTo(hall),
				() -> assertThat(existingSession.getBookings()).isNotNull(),
				() -> assertThat(existingSession.getBookedSeats()).isNotNull());
	}

	@ParameterizedTest
	@CsvSource({ "SCHEDULED, true", "COMPLETED, true", "CANCELLED, true" })
	void consistencyCheck_BothResponsesShouldHaveSameStatus(String statusStr, boolean expected) {
		CinemaSessionStatus status = CinemaSessionStatus.valueOf(statusStr);
		session.setStatus(status);

		SessionAdminResponse adminResponse = sessionMapper.toSessionAdminResponse(session);
		SessionScheduleResponse scheduleResponse = sessionMapper.toSessionScheduleResponse(session);

		assertThat(adminResponse.getStatus()).isEqualTo(scheduleResponse.getStatus());
		assertThat(adminResponse.getStatus()).isEqualTo(status);
	}

	@Test
	void consistencyCheck_BothResponsesShouldHaveSameMovieInfo() {
		SessionAdminResponse adminResponse = sessionMapper.toSessionAdminResponse(session);
		SessionScheduleResponse scheduleResponse = sessionMapper.toSessionScheduleResponse(session);

		assertAll(() -> assertThat(adminResponse.getMovieId()).isEqualTo(scheduleResponse.getMovieId()),
				() -> assertThat(adminResponse.getMovieTitle()).isEqualTo(scheduleResponse.getMovieTitle()),
				() -> assertThat(adminResponse.getMovieDuration()).isEqualTo(scheduleResponse.getMovieDuration()));
	}

	@Test
	void consistencyCheck_BothResponsesShouldHaveSameHallInfo() {
		SessionAdminResponse adminResponse = sessionMapper.toSessionAdminResponse(session);
		SessionScheduleResponse scheduleResponse = sessionMapper.toSessionScheduleResponse(session);

		assertAll(() -> assertThat(adminResponse.getHallId()).isEqualTo(scheduleResponse.getHallId()),
				() -> assertThat(adminResponse.getHallName()).isEqualTo(scheduleResponse.getHallName()));
	}
}