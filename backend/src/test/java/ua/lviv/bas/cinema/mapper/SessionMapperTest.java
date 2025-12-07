package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.session.request.SessionRequest;
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
				.hall(hall).build();
	}

	@Test
	void toAdminDto_ShouldMapAllFields() {
		SessionAdminResponse result = sessionMapper.toAdminDto(session);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getStartTime()).isEqualTo(futureTime);
		assertThat(result.getBasePrice()).isEqualByComparingTo("250.00");
		assertThat(result.getMovieId()).isEqualTo(1L);
		assertThat(result.getMovieTitle()).isEqualTo("Test Movie");
		assertThat(result.getMovieDuration()).isEqualTo(120);
		assertThat(result.getHallId()).isEqualTo(1L);
		assertThat(result.getHallName()).isEqualTo("Hall 1");
		assertThat(result.getHallCapacity()).isEqualTo(100);
		assertThat(result.getEndTime()).isNull();
		assertThat(result.isAvailable()).isFalse();
		assertThat(result.getTicketsSold()).isNull();
		assertThat(result.getTotalRevenue()).isNull();
	}

	@Test
	void toAdminDto_ShouldHandleNullHallCapacity() {
		hall.setSeats(null);
		SessionAdminResponse result = sessionMapper.toAdminDto(session);

		assertThat(result.getHallCapacity()).isEqualTo(0);
	}

	@Test
	void toAdminDto_ShouldHandleEmptyHallSeats() {
		hall.setSeats(new ArrayList<>());
		SessionAdminResponse result = sessionMapper.toAdminDto(session);

		assertThat(result.getHallCapacity()).isEqualTo(0);
	}

	@Test
	void toAdminDto_ShouldHandleNullMovie() {
		session.setMovie(null);
		SessionAdminResponse result = sessionMapper.toAdminDto(session);

		assertThat(result.getMovieId()).isNull();
		assertThat(result.getMovieTitle()).isNull();
		assertThat(result.getMovieDuration()).isNull();
	}

	@Test
	void toAdminDto_ShouldHandleNullBasePrice() {
		session.setBasePrice(null);
		SessionAdminResponse result = sessionMapper.toAdminDto(session);

		assertThat(result.getBasePrice()).isNull();
	}

	@Test
	void toAdminDto_ShouldHandleNullStartTime() {
		session.setStartTime(null);
		SessionAdminResponse result = sessionMapper.toAdminDto(session);

		assertThat(result.getStartTime()).isNull();
	}

	@Test
	void toAdminDto_ShouldReturnNull_WhenInputIsNull() {
		SessionAdminResponse result = sessionMapper.toAdminDto(null);

		assertThat(result).isNull();
	}

	@Test
	void toAdminDto_ShouldMapSessionWithoutBuilder() {
		Session session = new Session();
		session.setId(2L);
		session.setStartTime(futureTime);
		session.setBasePrice(new BigDecimal("300.00"));

		SessionAdminResponse result = sessionMapper.toAdminDto(session);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(2L);
		assertThat(result.getStartTime()).isEqualTo(futureTime);
		assertThat(result.getBasePrice()).isEqualByComparingTo("300.00");
	}

	@ParameterizedTest
	@CsvSource({ "100.00, 100.00", "0.00, 0.00", "999.99, 999.99" })
	void toAdminDto_ShouldMapDifferentBasePrices(String inputPrice, String expectedPrice) {
		session.setBasePrice(new BigDecimal(inputPrice));
		SessionAdminResponse result = sessionMapper.toAdminDto(session);

		assertThat(result.getBasePrice()).isEqualByComparingTo(expectedPrice);
	}

	@Test
	void toScheduleDto_ShouldMapAllFields() {
		SessionScheduleResponse result = sessionMapper.toScheduleDto(session);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getStartTime()).isEqualTo(futureTime);
		assertThat(result.getBasePrice()).isEqualByComparingTo("250.00");
		assertThat(result.getMovieId()).isEqualTo(1L);
		assertThat(result.getMovieTitle()).isEqualTo("Test Movie");
		assertThat(result.getMoviePosterFileName()).isEqualTo("poster.jpg");
		assertThat(result.getMovieAgeRating()).isEqualTo("PEGI_12");
		assertThat(result.getMovieDuration()).isEqualTo(120);
		assertThat(result.getHallId()).isEqualTo(1L);
		assertThat(result.getHallName()).isEqualTo("Hall 1");
		assertThat(result.getEndTime()).isNull();
		assertThat(result.getAvailableSeats()).isNull();
		assertThat(result.getHallCapacity()).isNull();
	}

	@Test
	void toScheduleDto_ShouldHandleNullMovie() {
		session.setMovie(null);
		SessionScheduleResponse result = sessionMapper.toScheduleDto(session);

		assertThat(result.getMovieId()).isNull();
		assertThat(result.getMovieTitle()).isNull();
		assertThat(result.getMoviePosterFileName()).isNull();
		assertThat(result.getMovieAgeRating()).isNull();
		assertThat(result.getMovieDuration()).isNull();
	}

	@Test
	void toScheduleDto_ShouldHandleNullHall() {
		session.setHall(null);
		SessionScheduleResponse result = sessionMapper.toScheduleDto(session);

		assertThat(result.getHallId()).isNull();
		assertThat(result.getHallName()).isNull();
	}

	@Test
	void toScheduleDto_ShouldHandleMovieWithoutPoster() {
		movie.setPosterFileName(null);
		SessionScheduleResponse result = sessionMapper.toScheduleDto(session);

		assertThat(result.getMoviePosterFileName()).isNull();
	}

	@Test
	void toScheduleDto_ShouldHandleMovieWithoutAgeRating() {
		movie.setAgeRating(null);
		SessionScheduleResponse result = sessionMapper.toScheduleDto(session);

		assertThat(result.getMovieAgeRating()).isNull();
	}

	@ParameterizedTest
	@ValueSource(ints = { 60, 90, 120, 150, 180 })
	void toScheduleDto_ShouldMapDifferentMovieDurations(int duration) {
		movie.setDurationMinutes(duration);
		SessionScheduleResponse result = sessionMapper.toScheduleDto(session);

		assertThat(result.getMovieDuration()).isEqualTo(duration);
	}

	@Test
	void toScheduleDto_ShouldReturnNull_WhenInputIsNull() {
		SessionScheduleResponse result = sessionMapper.toScheduleDto(null);

		assertThat(result).isNull();
	}

	@Test
	void toEntity_ShouldMapRequestToEntity() {
		SessionRequest request = SessionRequest.builder().startTime(LocalDateTime.now().plusHours(3))
				.basePrice(new BigDecimal("300.00")).movieId(2L).hallId(3L).build();

		Session result = sessionMapper.toEntity(request);

		assertThat(result).isNotNull();
		assertThat(result.getStartTime()).isEqualTo(request.getStartTime());
		assertThat(result.getBasePrice()).isEqualByComparingTo("300.00");
		assertThat(result.getId()).isNull();
		assertThat(result.getMovie()).isNull();
		assertThat(result.getHall()).isNull();
		assertThat(result.getTickets()).isNotNull();
		assertThat(result.getTickets()).isEmpty();
	}

	@Test
	void toEntity_ShouldHandleNullBasePrice() {
		SessionRequest request = SessionRequest.builder().startTime(futureTime).basePrice(null).movieId(1L).hallId(1L)
				.build();

		Session result = sessionMapper.toEntity(request);

		assertThat(result.getBasePrice()).isNull();
	}

	@Test
	void toEntity_ShouldHandleNullStartTime() {
		SessionRequest request = SessionRequest.builder().startTime(null).basePrice(new BigDecimal("200.00"))
				.movieId(1L).hallId(1L).build();

		Session result = sessionMapper.toEntity(request);

		assertThat(result.getStartTime()).isNull();
	}

	@Test
	void toEntity_ShouldHandleNullIds() {
		SessionRequest request = SessionRequest.builder().startTime(futureTime).basePrice(new BigDecimal("200.00"))
				.movieId(null).hallId(null).build();

		Session result = sessionMapper.toEntity(request);

		assertThat(result.getStartTime()).isEqualTo(futureTime);
		assertThat(result.getBasePrice()).isEqualByComparingTo("200.00");
	}

	@Test
	void toEntity_ShouldReturnNull_WhenRequestIsNull() {
		Session result = sessionMapper.toEntity(null);

		assertThat(result).isNull();
	}

	@Test
	void toEntity_ShouldMapZeroBasePrice() {
		SessionRequest request = SessionRequest.builder().startTime(futureTime).basePrice(BigDecimal.ZERO).movieId(1L)
				.hallId(1L).build();

		Session result = sessionMapper.toEntity(request);

		assertThat(result.getBasePrice()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void toEntity_ShouldMapFutureDates() {
		LocalDateTime[] futureDates = { LocalDateTime.now().plusHours(1), LocalDateTime.now().plusDays(1),
				LocalDateTime.now().plusMonths(1), LocalDateTime.now().plusYears(1) };

		for (LocalDateTime date : futureDates) {
			SessionRequest request = SessionRequest.builder().startTime(date).basePrice(new BigDecimal("100.00"))
					.movieId(1L).hallId(1L).build();

			Session result = sessionMapper.toEntity(request);

			assertThat(result.getStartTime()).isEqualTo(date);
		}
	}

	@Test
	void consistencyCheck_AdminDtoShouldContainCorrectMovieInfo() {
		SessionAdminResponse adminDto = sessionMapper.toAdminDto(session);

		assertThat(adminDto.getMovieTitle()).isEqualTo("Test Movie");
		assertThat(adminDto.getMovieDuration()).isEqualTo(120);
	}

	@Test
	void consistencyCheck_ScheduleDtoShouldContainCorrectHallInfo() {
		SessionScheduleResponse scheduleDto = sessionMapper.toScheduleDto(session);

		assertThat(scheduleDto.getHallName()).isEqualTo("Hall 1");
		assertThat(scheduleDto.getHallId()).isEqualTo(1L);
	}

	@Test
	void consistencyCheck_BothDtosShouldHaveSameBaseData() {
		SessionAdminResponse adminDto = sessionMapper.toAdminDto(session);
		SessionScheduleResponse scheduleDto = sessionMapper.toScheduleDto(session);

		assertThat(adminDto.getId()).isEqualTo(scheduleDto.getId());
		assertThat(adminDto.getStartTime()).isEqualTo(scheduleDto.getStartTime());
		assertThat(adminDto.getBasePrice()).isEqualTo(scheduleDto.getBasePrice());
		assertThat(adminDto.getMovieId()).isEqualTo(scheduleDto.getMovieId());
		assertThat(adminDto.getMovieTitle()).isEqualTo(scheduleDto.getMovieTitle());
		assertThat(adminDto.getMovieDuration()).isEqualTo(scheduleDto.getMovieDuration());
	}

	@Test
	void toEntity_ShouldNotHaveSideEffectsOnRequest() {
		SessionRequest originalRequest = SessionRequest.builder().startTime(futureTime)
				.basePrice(new BigDecimal("400.00")).movieId(5L).hallId(6L).build();

		SessionRequest requestCopy = SessionRequest.builder().startTime(originalRequest.getStartTime())
				.basePrice(originalRequest.getBasePrice()).movieId(originalRequest.getMovieId())
				.hallId(originalRequest.getHallId()).build();

		sessionMapper.toEntity(originalRequest);

		assertThat(originalRequest.getStartTime()).isEqualTo(requestCopy.getStartTime());
		assertThat(originalRequest.getBasePrice()).isEqualTo(requestCopy.getBasePrice());
		assertThat(originalRequest.getMovieId()).isEqualTo(requestCopy.getMovieId());
		assertThat(originalRequest.getHallId()).isEqualTo(requestCopy.getHallId());
	}
}