package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.dto.session.request.SessionRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;

class SessionMapperTest {

	private SessionMapper sessionMapper = Mappers.getMapper(SessionMapper.class);

	@Test
	void toAdminDto_ShouldMapSessionToAdminDto() {
		LocalDateTime futureTime = LocalDateTime.now().plusHours(2);

		Movie movie = Movie.builder().id(1L).title("Test Movie").durationMinutes(120).build();

		CinemaHall hall = CinemaHall.builder().id(1L).name("Hall 1").build();

		Session session = Session.builder().id(1L).startTime(futureTime).basePrice(new BigDecimal("250.00"))
				.movie(movie).hall(hall).build();

		SessionAdminResponse result = sessionMapper.toAdminDto(session);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getStartTime()).isEqualTo(futureTime);
		assertThat(result.getBasePrice()).isEqualTo(new BigDecimal("250.00"));
		assertThat(result.getMovieId()).isEqualTo(1L);
		assertThat(result.getMovieTitle()).isEqualTo("Test Movie");
		assertThat(result.getMovieDuration()).isEqualTo(120);
		assertThat(result.getHallId()).isEqualTo(1L);
		assertThat(result.getHallName()).isEqualTo("Hall 1");
		assertThat(result.getHallCapacity()).isEqualTo(0);
		assertThat(result.getEndTime()).isNull();
		assertThat(result.isAvailable()).isFalse();
		assertThat(result.getTicketsSold()).isNull();
		assertThat(result.getTotalRevenue()).isNull();
	}

	@Test
	void toScheduleDto_ShouldMapSessionToScheduleDto() {
		LocalDateTime futureTime = LocalDateTime.now().plusHours(2);

		Movie movie = Movie.builder().id(1L).title("Test Movie").durationMinutes(120).posterFileName("poster.jpg")
				.build();

		CinemaHall hall = CinemaHall.builder().id(1L).name("Hall 1").build();

		Session session = Session.builder().id(1L).startTime(futureTime).basePrice(new BigDecimal("250.00"))
				.movie(movie).hall(hall).build();

		SessionScheduleResponse result = sessionMapper.toScheduleDto(session);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getStartTime()).isEqualTo(futureTime);
		assertThat(result.getBasePrice()).isEqualTo(new BigDecimal("250.00"));
		assertThat(result.getMovieId()).isEqualTo(1L);
		assertThat(result.getMovieTitle()).isEqualTo("Test Movie");
		assertThat(result.getMoviePosterFileName()).isEqualTo("poster.jpg");
		assertThat(result.getMovieAgeRating()).isNull();
		assertThat(result.getMovieDuration()).isEqualTo(120);
		assertThat(result.getHallId()).isEqualTo(1L);
		assertThat(result.getHallName()).isEqualTo("Hall 1");
		assertThat(result.getEndTime()).isNull();
		assertThat(result.getAvailableSeats()).isNull();
		assertThat(result.getHallCapacity()).isNull();
	}

	@Test
	void toEntity_ShouldMapRequestToEntity() {
		SessionRequest request = SessionRequest.builder().startTime(LocalDateTime.now().plusHours(3))
				.basePrice(new BigDecimal("300.00")).movieId(2L).hallId(3L).build();

		Session result = sessionMapper.toEntity(request);

		assertThat(result).isNotNull();
		assertThat(result.getStartTime()).isEqualTo(request.getStartTime());
		assertThat(result.getBasePrice()).isEqualTo(new BigDecimal("300.00"));
		assertThat(result.getId()).isNull();
		assertThat(result.getMovie()).isNull();
		assertThat(result.getHall()).isNull();
		assertThat(result.getTickets()).isNotNull();
		assertThat(result.getTickets()).isEmpty();
	}

	@Test
	void toAdminDto_WithNull_ShouldReturnNull() {
		assertThat(sessionMapper.toAdminDto(null)).isNull();
	}

	@Test
	void toScheduleDto_WithNull_ShouldReturnNull() {
		assertThat(sessionMapper.toScheduleDto(null)).isNull();
	}

	@Test
	void toEntity_WithNull_ShouldReturnNull() {
		assertThat(sessionMapper.toEntity(null)).isNull();
	}
}