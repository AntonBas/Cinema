package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.dto.SessionDto;
import ua.lviv.bas.cinema.dto.SessionRequest;

class SessionMapperTest {

	private SessionMapper sessionMapper = Mappers.getMapper(SessionMapper.class);

	@Test
	void toDto_ShouldMapSessionToDto() {
		LocalDateTime futureTime = LocalDateTime.now().plusHours(2);

		Movie movie = Movie.builder().id(1L).title("Test Movie").durationMinutes(120).build();

		CinemaHall hall = CinemaHall.builder().id(1L).name("Hall 1").build();

		Session session = Session.builder().id(1L).startTime(futureTime).price(new BigDecimal("250.00")).movie(movie)
				.hall(hall).build();

		SessionDto result = sessionMapper.toDto(session);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getStartTime()).isEqualTo(futureTime);
		assertThat(result.getEndTime()).isEqualTo(futureTime.plusMinutes(120));
		assertThat(result.getPrice()).isEqualTo(new BigDecimal("250.00"));
		assertThat(result.isAvailable()).isTrue();
	}

	@Test
	void toDto_ShouldSetAvailableFalseForPastSession() {
		LocalDateTime pastTime = LocalDateTime.now().minusHours(2);

		Session session = Session.builder().id(2L).startTime(pastTime).price(new BigDecimal("200.00"))
				.movie(Movie.builder().id(1L).title("Movie").durationMinutes(120).build())
				.hall(CinemaHall.builder().id(1L).name("Hall 1").build()).build();

		SessionDto result = sessionMapper.toDto(session);

		assertThat(result.isAvailable()).isFalse();
	}

	@Test
	void toEntity_ShouldMapRequestToEntity() {
		SessionRequest request = SessionRequest.builder().startTime(LocalDateTime.now().plusHours(3))
				.price(new BigDecimal("300.00")).movieId(2L).hallId(3L).build();

		Session result = sessionMapper.toEntity(request);

		assertThat(result).isNotNull();
		assertThat(result.getPrice()).isEqualTo(new BigDecimal("300.00"));
		assertThat(result.getId()).isNull();
		assertThat(result.getMovie()).isNull();
		assertThat(result.getHall()).isNull();
	}

	@Test
	void toDto_WithNull_ShouldReturnNull() {
		assertThat(sessionMapper.toDto(null)).isNull();
	}

	@Test
	void toEntity_WithNull_ShouldReturnNull() {
		assertThat(sessionMapper.toEntity(null)).isNull();
	}
}