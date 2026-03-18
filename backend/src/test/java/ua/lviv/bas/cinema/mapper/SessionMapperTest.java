package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.projection.SessionAdminProjection;
import ua.lviv.bas.cinema.domain.projection.SessionScheduleProjection;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;

public class SessionMapperTest {

	private SessionMapper mapper = Mappers.getMapper(SessionMapper.class);
	private LocalDateTime fixedTime;

	@BeforeEach
	void setUp() {
		fixedTime = LocalDateTime.of(2026, 3, 18, 20, 0, 0);
	}

	@Test
	void toAdminResponseFromSession() {
		Movie movie = Movie.builder().id(1L).title("Test Movie").durationMinutes(120).build();
		CinemaHall hall = CinemaHall.builder().id(1L).name("Hall 1").build();
		Session session = Session.builder().id(1L).startTime(fixedTime).basePrice(new BigDecimal("250.00")).movie(movie)
				.hall(hall).status(CinemaSessionStatus.SCHEDULED).build();

		SessionAdminResponse response = mapper.toAdminResponse(session);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getStartTime()).isEqualTo(fixedTime);
		assertThat(response.getEndTime()).isEqualTo(fixedTime.plusMinutes(120));
		assertThat(response.getBasePrice()).isEqualTo(new BigDecimal("250.00"));
		assertThat(response.getMovieId()).isEqualTo(1L);
		assertThat(response.getMovieTitle()).isEqualTo("Test Movie");
		assertThat(response.getMovieDuration()).isEqualTo(120);
		assertThat(response.getHallId()).isEqualTo(1L);
		assertThat(response.getHallName()).isEqualTo("Hall 1");
		assertThat(response.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
	}

	@Test
	void toAdminResponseFromSessionWithNullMovieAndHall() {
		Session session = Session.builder().id(1L).startTime(fixedTime).basePrice(new BigDecimal("250.00"))
				.status(CinemaSessionStatus.SCHEDULED).build();

		SessionAdminResponse response = mapper.toAdminResponse(session);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getStartTime()).isEqualTo(fixedTime);
		assertThat(response.getBasePrice()).isEqualTo(new BigDecimal("250.00"));
		assertThat(response.getEndTime()).isNull();
		assertThat(response.getMovieId()).isNull();
		assertThat(response.getMovieTitle()).isNull();
		assertThat(response.getMovieDuration()).isNull();
		assertThat(response.getHallId()).isNull();
		assertThat(response.getHallName()).isNull();
	}

	@Test
	void toAdminResponseFromProjection() {
		SessionAdminProjection projection = null;
		SessionAdminResponse response = mapper.toAdminResponse(projection);
		assertThat(response).isNull();
	}

	@Test
	void toScheduleResponseFromSession() {
		Movie movie = Movie.builder().id(1L).title("Schedule Movie").posterFileName("poster.jpg")
				.ageRating(AgeRating.PEGI_12).durationMinutes(90).build();
		CinemaHall hall = CinemaHall.builder().id(1L).name("Hall 2").build();
		Session session = Session.builder().id(1L).startTime(fixedTime).basePrice(new BigDecimal("200.00")).movie(movie)
				.hall(hall).status(CinemaSessionStatus.SCHEDULED).build();

		SessionScheduleResponse response = mapper.toScheduleResponse(session);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getStartTime()).isEqualTo(fixedTime);
		assertThat(response.getEndTime()).isEqualTo(fixedTime.plusMinutes(90));
		assertThat(response.getBasePrice()).isEqualTo(new BigDecimal("200.00"));
		assertThat(response.getMovieId()).isEqualTo(1L);
		assertThat(response.getMovieTitle()).isEqualTo("Schedule Movie");
		assertThat(response.getMoviePosterFileName()).isEqualTo("poster.jpg");
		assertThat(response.getMovieAgeRating()).isEqualTo(AgeRating.PEGI_12.name());
		assertThat(response.getMovieDuration()).isEqualTo(90);
		assertThat(response.getHallId()).isEqualTo(1L);
		assertThat(response.getHallName()).isEqualTo("Hall 2");
		assertThat(response.getHallCapacity()).isNull();
		assertThat(response.getAvailableSeats()).isNull();
	}

	@Test
	void toScheduleResponseFromSessionWithNullFields() {
		Session session = Session.builder().id(1L).startTime(fixedTime).basePrice(new BigDecimal("200.00"))
				.status(CinemaSessionStatus.SCHEDULED).build();

		SessionScheduleResponse response = mapper.toScheduleResponse(session);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getStartTime()).isEqualTo(fixedTime);
		assertThat(response.getBasePrice()).isEqualTo(new BigDecimal("200.00"));
		assertThat(response.getEndTime()).isNull();
		assertThat(response.getMovieId()).isNull();
		assertThat(response.getMovieTitle()).isNull();
		assertThat(response.getMoviePosterFileName()).isNull();
		assertThat(response.getMovieAgeRating()).isNull();
		assertThat(response.getMovieDuration()).isNull();
		assertThat(response.getHallId()).isNull();
		assertThat(response.getHallName()).isNull();
		assertThat(response.getHallCapacity()).isNull();
		assertThat(response.getAvailableSeats()).isNull();
	}

	@Test
	void toScheduleResponseFromProjection() {
		SessionScheduleProjection projection = null;
		SessionScheduleResponse response = mapper.toScheduleResponse(projection);
		assertThat(response).isNull();
	}

	@Test
	void toEntityFromCreateRequest() {
		LocalDateTime startTime = fixedTime.plusDays(1);
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
	void toEntityFromNullRequest() {
		Session session = mapper.toEntity(null);
		assertThat(session).isNull();
	}

	@Test
	void updateEntityWithNullRequest() {
		Session session = Session.builder().id(1L).startTime(fixedTime).basePrice(new BigDecimal("250.00"))
				.status(CinemaSessionStatus.SCHEDULED).build();

		mapper.updateEntity(session, null);

		assertThat(session.getStartTime()).isEqualTo(fixedTime);
		assertThat(session.getBasePrice()).isEqualTo(new BigDecimal("250.00"));
		assertThat(session.getId()).isEqualTo(1L);
		assertThat(session.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
	}

	@Test
	void nullHandling() {
		assertThat(mapper.toAdminResponse((Session) null)).isNull();
		assertThat(mapper.toScheduleResponse((Session) null)).isNull();
		assertThat(mapper.toEntity(null)).isNull();
	}
}