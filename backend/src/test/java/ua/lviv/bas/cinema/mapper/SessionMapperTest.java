package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionUpdateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;

public class SessionMapperTest {

	private SessionMapper mapper = Mappers.getMapper(SessionMapper.class);

	@Test
	void toSessionAdminResponse() {
		Movie movie = Movie.builder().id(1L).title("Test Movie").durationMinutes(120).build();

		CinemaHall hall = CinemaHall.builder().id(1L).name("Hall 1").build();

		Session session = Session.builder().id(1L).startTime(LocalDateTime.now()).basePrice(new BigDecimal("250.00"))
				.movie(movie).hall(hall).status(CinemaSessionStatus.SCHEDULED).build();

		SessionAdminResponse response = mapper.toSessionAdminResponse(session);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getBasePrice()).isEqualTo(new BigDecimal("250.00"));
		assertThat(response.getMovieId()).isEqualTo(1L);
		assertThat(response.getMovieTitle()).isEqualTo("Test Movie");
		assertThat(response.getHallId()).isEqualTo(1L);
		assertThat(response.getHallName()).isEqualTo("Hall 1");
		assertThat(response.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
	}

	@Test
	void toSessionAdminResponseFromProjection() {
		var projection = Mockito.mock(ua.lviv.bas.cinema.domain.projection.SessionAdminProjection.class);
		Mockito.when(projection.getId()).thenReturn(1L);
		Mockito.when(projection.getStartTime()).thenReturn(LocalDateTime.now());
		Mockito.when(projection.getBasePrice()).thenReturn(new BigDecimal("300.00"));
		Mockito.when(projection.getMovieTitle()).thenReturn("Projection Movie");
		Mockito.when(projection.getHallName()).thenReturn("Projection Hall");

		SessionAdminResponse response = mapper.toSessionAdminResponse(projection);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getBasePrice()).isEqualTo(new BigDecimal("300.00"));
		assertThat(response.getMovieTitle()).isEqualTo("Projection Movie");
		assertThat(response.getHallName()).isEqualTo("Projection Hall");
	}

	@Test
	void toSessionScheduleResponse() {
		Movie movie = Movie.builder().id(1L).title("Schedule Movie").posterFileName("poster.jpg")
				.ageRating(AgeRating.PEGI_12).durationMinutes(90).build();

		CinemaHall hall = CinemaHall.builder().id(1L).name("Hall 2").build();

		Session session = Session.builder().id(1L).startTime(LocalDateTime.now()).basePrice(new BigDecimal("200.00"))
				.movie(movie).hall(hall).status(CinemaSessionStatus.SCHEDULED).build();

		SessionScheduleResponse response = mapper.toSessionScheduleResponse(session);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getBasePrice()).isEqualTo(new BigDecimal("200.00"));
		assertThat(response.getMovieId()).isEqualTo(1L);
		assertThat(response.getMovieTitle()).isEqualTo("Schedule Movie");
		assertThat(response.getMoviePosterFileName()).isEqualTo("poster.jpg");
		assertThat(response.getHallId()).isEqualTo(1L);
		assertThat(response.getHallName()).isEqualTo("Hall 2");
	}

	@Test
	void toSession() {
		SessionCreateRequest request = SessionCreateRequest.builder().startTime(LocalDateTime.now().plusDays(1))
				.basePrice(new BigDecimal("300.00")).movieId(1L).hallId(2L).build();

		Session session = mapper.toSession(request);

		assertThat(session.getStartTime()).isEqualTo(request.getStartTime());
		assertThat(session.getBasePrice()).isEqualTo(new BigDecimal("300.00"));
		assertThat(session.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
	}

	@Test
	void updateSessionFromRequest() {
		Session session = new Session();
		session.setId(1L);
		session.setStartTime(LocalDateTime.now());
		session.setBasePrice(new BigDecimal("250.00"));
		session.setStatus(CinemaSessionStatus.SCHEDULED);

		SessionUpdateRequest request = SessionUpdateRequest.builder().startTime(LocalDateTime.now().plusHours(1))
				.basePrice(new BigDecimal("350.00")).build();

		mapper.updateSessionFromRequest(request, session);

		assertThat(session.getStartTime()).isEqualTo(request.getStartTime());
		assertThat(session.getBasePrice()).isEqualTo(new BigDecimal("350.00"));
	}

	@Test
	void toSessionAdminResponseWithNull() {
		SessionAdminResponse response = mapper.toSessionAdminResponse((Session) null);
		assertThat(response).isNull();
	}

	@Test
	void toSessionScheduleResponseWithNull() {
		SessionScheduleResponse response = mapper.toSessionScheduleResponse((Session) null);
		assertThat(response).isNull();
	}

	@Test
	void toSessionWithNull() {
		Session session = mapper.toSession(null);
		assertThat(session).isNull();
	}
}