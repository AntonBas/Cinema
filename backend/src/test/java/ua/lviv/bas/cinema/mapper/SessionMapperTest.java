package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

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
	void toAdminResponseFromSession() {
		Movie movie = Movie.builder().id(1L).title("Test Movie").durationMinutes(120).build();

		CinemaHall hall = CinemaHall.builder().id(1L).name("Hall 1").build();

		LocalDateTime startTime = LocalDateTime.now();
		Session session = Session.builder().id(1L).startTime(startTime).basePrice(new BigDecimal("250.00")).movie(movie)
				.hall(hall).status(CinemaSessionStatus.SCHEDULED).build();

		SessionAdminResponse response = mapper.toAdminResponse(session);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getStartTime()).isEqualTo(startTime);
		assertThat(response.getEndTime()).isEqualTo(startTime.plusMinutes(120));
		assertThat(response.getBasePrice()).isEqualTo(new BigDecimal("250.00"));
		assertThat(response.getMovieId()).isEqualTo(1L);
		assertThat(response.getMovieTitle()).isEqualTo("Test Movie");
		assertThat(response.getMovieDuration()).isEqualTo(120);
		assertThat(response.getHallId()).isEqualTo(1L);
		assertThat(response.getHallName()).isEqualTo("Hall 1");
		assertThat(response.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
	}

	@Test
	void toAdminResponseFromProjection() {
		SessionAdminResponse response = mapper
				.toAdminResponse((ua.lviv.bas.cinema.domain.projection.SessionAdminProjection) null);
		assertThat(response).isNull();
	}

	@Test
	void toScheduleResponseFromSession() {
		Movie movie = Movie.builder().id(1L).title("Schedule Movie").posterFileName("poster.jpg")
				.ageRating(AgeRating.PEGI_12).durationMinutes(90).build();

		CinemaHall hall = CinemaHall.builder().id(1L).name("Hall 2").build();

		LocalDateTime startTime = LocalDateTime.now();
		Session session = Session.builder().id(1L).startTime(startTime).basePrice(new BigDecimal("200.00")).movie(movie)
				.hall(hall).status(CinemaSessionStatus.SCHEDULED).build();

		SessionScheduleResponse response = mapper.toScheduleResponse(session);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getStartTime()).isEqualTo(startTime);
		assertThat(response.getEndTime()).isEqualTo(startTime.plusMinutes(90));
		assertThat(response.getBasePrice()).isEqualTo(new BigDecimal("200.00"));
		assertThat(response.getMovieId()).isEqualTo(1L);
		assertThat(response.getMovieTitle()).isEqualTo("Schedule Movie");
		assertThat(response.getMoviePosterFileName()).isEqualTo("poster.jpg");
		assertThat(response.getMovieAgeRating()).isEqualTo(AgeRating.PEGI_12.name());
		assertThat(response.getMovieDuration()).isEqualTo(90);
		assertThat(response.getHallId()).isEqualTo(1L);
		assertThat(response.getHallName()).isEqualTo("Hall 2");
		assertThat(response.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
	}

	@Test
	void toScheduleResponseFromProjection() {
		SessionScheduleResponse response = mapper
				.toScheduleResponse((ua.lviv.bas.cinema.domain.projection.SessionScheduleProjection) null);
		assertThat(response).isNull();
	}

	@Test
	void toEntityFromCreateRequest() {
		LocalDateTime startTime = LocalDateTime.now().plusDays(1);
		SessionCreateRequest request = SessionCreateRequest.builder().startTime(startTime)
				.basePrice(new BigDecimal("300.00")).movieId(1L).hallId(2L).build();

		Session session = mapper.toEntity(request);

		assertThat(session.getStartTime()).isEqualTo(startTime);
		assertThat(session.getBasePrice()).isEqualTo(new BigDecimal("300.00"));
		assertThat(session.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
		assertThat(session.getId()).isNull();
		assertThat(session.getMovie()).isNull();
		assertThat(session.getHall()).isNull();
	}

	@Test
	void updateEntityFromRequest() {
		LocalDateTime originalTime = LocalDateTime.now();
		LocalDateTime updatedTime = originalTime.plusHours(1);

		Session session = Session.builder().id(1L).startTime(originalTime).basePrice(new BigDecimal("250.00"))
				.status(CinemaSessionStatus.SCHEDULED).build();

		SessionUpdateRequest request = SessionUpdateRequest.builder().startTime(updatedTime)
				.basePrice(new BigDecimal("350.00")).build();

		mapper.updateEntity(session, request);

		assertThat(session.getStartTime()).isEqualTo(updatedTime);
		assertThat(session.getBasePrice()).isEqualTo(new BigDecimal("350.00"));
		assertThat(session.getId()).isEqualTo(1L);
		assertThat(session.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
	}

	@Test
	void updateEntityWithNullRequest() {
		Session session = Session.builder().id(1L).startTime(LocalDateTime.now()).basePrice(new BigDecimal("250.00"))
				.build();

		mapper.updateEntity(session, null);

		assertThat(session.getStartTime()).isNotNull();
		assertThat(session.getBasePrice()).isEqualTo(new BigDecimal("250.00"));
	}

	@Test
	void nullHandling() {
		assertThat(mapper.toAdminResponse((Session) null)).isNull();
		assertThat(mapper.toScheduleResponse((Session) null)).isNull();
		assertThat(mapper.toEntity(null)).isNull();
	}
}