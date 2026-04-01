package ua.lviv.bas.cinema.mapper.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.enums.AgeRating;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.session.request.SessionCreateRequest;
import ua.lviv.bas.cinema.dto.session.request.SessionUpdateRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.repository.cinema.projection.SessionAdminProjection;
import ua.lviv.bas.cinema.repository.cinema.projection.SessionScheduleProjection;

public class SessionMapperTest {

	private SessionMapper mapper = Mappers.getMapper(SessionMapper.class);
	private LocalDateTime fixedTime;

	@BeforeEach
	void setUp() {
		fixedTime = LocalDateTime.of(2026, 3, 18, 20, 0, 0);
	}

	@Test
	void toAdminResponseFromProjection_ShouldMapAllFields() {
		SessionAdminProjection projection = new SessionAdminProjection(1L, java.sql.Timestamp.valueOf(fixedTime),
				java.sql.Timestamp.valueOf(fixedTime.plusMinutes(120)), new BigDecimal("250.00"), "SCHEDULED", 1L,
				"Test Movie", 120, 1L, "Hall 1", 150L, 45L, new BigDecimal("6750.00"));

		SessionAdminResponse response = mapper.toAdminResponse(projection);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.startTime()).isEqualTo(fixedTime);
		assertThat(response.endTime()).isEqualTo(fixedTime.plusMinutes(120));
		assertThat(response.basePrice()).isEqualTo(new BigDecimal("250.00"));
		assertThat(response.status()).isEqualTo(CinemaSessionStatus.SCHEDULED);
		assertThat(response.movieId()).isEqualTo(1L);
		assertThat(response.movieTitle()).isEqualTo("Test Movie");
		assertThat(response.movieDuration()).isEqualTo(120);
		assertThat(response.hallId()).isEqualTo(1L);
		assertThat(response.hallName()).isEqualTo("Hall 1");
		assertThat(response.hallCapacity()).isEqualTo(150);
		assertThat(response.ticketsSold()).isEqualTo(45);
		assertThat(response.totalRevenue()).isEqualTo(new BigDecimal("6750.00"));
	}

	@Test
	void toAdminResponseFromProjection_WithNullValues_ShouldMapNull() {
		SessionAdminProjection projection = new SessionAdminProjection(1L, null, null, null, null, null, null, null,
				null, null, null, null, null);

		SessionAdminResponse response = mapper.toAdminResponse(projection);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.startTime()).isNull();
		assertThat(response.endTime()).isNull();
		assertThat(response.basePrice()).isNull();
		assertThat(response.status()).isNull();
		assertThat(response.movieId()).isNull();
		assertThat(response.movieTitle()).isNull();
		assertThat(response.movieDuration()).isNull();
		assertThat(response.hallId()).isNull();
		assertThat(response.hallName()).isNull();
		assertThat(response.hallCapacity()).isNull();
		assertThat(response.ticketsSold()).isNull();
		assertThat(response.totalRevenue()).isNull();
	}

	@Test
	void toAdminResponseFromProjection_WithNullProjection_ShouldReturnNull() {
		SessionAdminResponse response = mapper.toAdminResponse((SessionAdminProjection) null);
		assertThat(response).isNull();
	}

	@Test
	void toScheduleResponseFromProjection_ShouldMapAllFields() {
		SessionScheduleProjection projection = new SessionScheduleProjection() {
			@Override
			public Long getId() {
				return 1L;
			}

			@Override
			public LocalDateTime getStartTime() {
				return fixedTime;
			}

			@Override
			public LocalDateTime getEndTime() {
				return fixedTime.plusMinutes(90);
			}

			@Override
			public BigDecimal getBasePrice() {
				return new BigDecimal("200.00");
			}

			@Override
			public Long getMovieId() {
				return 1L;
			}

			@Override
			public String getMovieTitle() {
				return "Schedule Movie";
			}

			@Override
			public String getMoviePosterFileName() {
				return "poster.jpg";
			}

			@Override
			public String getMovieAgeRating() {
				return AgeRating.PEGI_12.name();
			}

			@Override
			public Integer getMovieDuration() {
				return 90;
			}

			@Override
			public Long getHallId() {
				return 1L;
			}

			@Override
			public String getHallName() {
				return "Hall 2";
			}

			@Override
			public Integer getHallCapacity() {
				return 150;
			}

			@Override
			public Integer getAvailableSeats() {
				return 105;
			}
		};

		SessionScheduleResponse response = mapper.toScheduleResponse(projection);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.startTime()).isEqualTo(fixedTime);
		assertThat(response.endTime()).isEqualTo(fixedTime.plusMinutes(90));
		assertThat(response.basePrice()).isEqualTo(new BigDecimal("200.00"));
		assertThat(response.movieId()).isEqualTo(1L);
		assertThat(response.movieTitle()).isEqualTo("Schedule Movie");
		assertThat(response.moviePosterFileName()).isEqualTo("poster.jpg");
		assertThat(response.movieAgeRating()).isEqualTo(AgeRating.PEGI_12.name());
		assertThat(response.movieDuration()).isEqualTo(90);
		assertThat(response.hallId()).isEqualTo(1L);
		assertThat(response.hallName()).isEqualTo("Hall 2");
		assertThat(response.hallCapacity()).isEqualTo(150);
		assertThat(response.availableSeats()).isEqualTo(105);
	}

	@Test
	void toScheduleResponseFromProjection_WithNullProjection_ShouldReturnNull() {
		SessionScheduleResponse response = mapper.toScheduleResponse((SessionScheduleProjection) null);
		assertThat(response).isNull();
	}

	@Test
	void toScheduleResponseFromSession_ShouldMapAllFields() {
		Movie movie = Movie.builder().id(1L).title("Schedule Movie").posterFileName("poster.jpg")
				.ageRating(AgeRating.PEGI_12).durationMinutes(90).build();
		CinemaHall hall = CinemaHall.builder().id(1L).name("Hall 2").build();
		Session session = Session.builder().id(1L).startTime(fixedTime).basePrice(new BigDecimal("200.00")).movie(movie)
				.hall(hall).status(CinemaSessionStatus.SCHEDULED).build();

		SessionScheduleResponse response = mapper.toScheduleResponse(session);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.startTime()).isEqualTo(fixedTime);
		assertThat(response.endTime()).isEqualTo(fixedTime.plusMinutes(90));
		assertThat(response.basePrice()).isEqualTo(new BigDecimal("200.00"));
		assertThat(response.movieId()).isEqualTo(1L);
		assertThat(response.movieTitle()).isEqualTo("Schedule Movie");
		assertThat(response.moviePosterFileName()).isEqualTo("poster.jpg");
		assertThat(response.movieAgeRating()).isEqualTo(AgeRating.PEGI_12.name());
		assertThat(response.movieDuration()).isEqualTo(90);
		assertThat(response.hallId()).isEqualTo(1L);
		assertThat(response.hallName()).isEqualTo("Hall 2");
		assertThat(response.hallCapacity()).isNull();
		assertThat(response.availableSeats()).isNull();
	}

	@Test
	void toScheduleResponseFromSession_WithNullMovie_ShouldReturnNullEndTime() {
		Session session = Session.builder().id(1L).startTime(fixedTime).basePrice(new BigDecimal("200.00"))
				.hall(CinemaHall.builder().id(1L).name("Hall 2").build()).status(CinemaSessionStatus.SCHEDULED).build();

		SessionScheduleResponse response = mapper.toScheduleResponse(session);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.startTime()).isEqualTo(fixedTime);
		assertThat(response.endTime()).isNull();
		assertThat(response.movieId()).isNull();
		assertThat(response.movieTitle()).isNull();
		assertThat(response.moviePosterFileName()).isNull();
		assertThat(response.movieAgeRating()).isNull();
		assertThat(response.movieDuration()).isNull();
	}

	@Test
	void toScheduleResponseFromSession_WithNullSession_ShouldReturnNull() {
		SessionScheduleResponse response = mapper.toScheduleResponse((Session) null);
		assertThat(response).isNull();
	}

	@Test
	void toEntityFromCreateRequest_ShouldMapFields() {
		LocalDateTime startTime = fixedTime.plusDays(1);
		SessionCreateRequest request = new SessionCreateRequest(startTime, new BigDecimal("300.00"), 1L, 2L);

		Session session = mapper.toEntity(request);

		assertThat(session.getStartTime()).isEqualTo(startTime);
		assertThat(session.getBasePrice()).isEqualTo(new BigDecimal("300.00"));
		assertThat(session.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
		assertThat(session.getId()).isNull();
		assertThat(session.getMovie()).isNull();
		assertThat(session.getHall()).isNull();
		assertThat(session.getBookings()).isNotNull();
		assertThat(session.getSeatReservations()).isNotNull();
	}

	@Test
	void toEntityFromCreateRequest_WithNullRequest_ShouldReturnNull() {
		Session session = mapper.toEntity((SessionCreateRequest) null);
		assertThat(session).isNull();
	}

	@Test
	void updateEntity_ShouldUpdateFields() {
		Session session = Session.builder().id(1L).startTime(fixedTime).basePrice(new BigDecimal("250.00"))
				.status(CinemaSessionStatus.SCHEDULED).build();

		LocalDateTime newStartTime = fixedTime.plusHours(1);
		SessionUpdateRequest request = new SessionUpdateRequest(newStartTime, new BigDecimal("300.00"), 2L, 3L);

		mapper.updateEntity(session, request);

		assertThat(session.getStartTime()).isEqualTo(newStartTime);
		assertThat(session.getBasePrice()).isEqualTo(new BigDecimal("300.00"));
		assertThat(session.getId()).isEqualTo(1L);
		assertThat(session.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
	}

	@Test
	void updateEntity_WithPartialUpdate_ShouldUpdateOnlyNonNullFields() {
		Session session = Session.builder().id(1L).startTime(fixedTime).basePrice(new BigDecimal("250.00"))
				.status(CinemaSessionStatus.SCHEDULED).build();

		SessionUpdateRequest request = new SessionUpdateRequest(null, new BigDecimal("300.00"), null, null);

		mapper.updateEntity(session, request);

		assertThat(session.getStartTime()).isEqualTo(fixedTime);
		assertThat(session.getBasePrice()).isEqualTo(new BigDecimal("300.00"));
		assertThat(session.getId()).isEqualTo(1L);
		assertThat(session.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
	}

	@Test
	void updateEntity_WithNullRequest_ShouldNotChange() {
		Session session = Session.builder().id(1L).startTime(fixedTime).basePrice(new BigDecimal("250.00"))
				.status(CinemaSessionStatus.SCHEDULED).build();

		mapper.updateEntity(session, null);

		assertThat(session.getStartTime()).isEqualTo(fixedTime);
		assertThat(session.getBasePrice()).isEqualTo(new BigDecimal("250.00"));
		assertThat(session.getId()).isEqualTo(1L);
		assertThat(session.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
	}

	@Test
	void updateEntity_WithNullTarget_ShouldThrowException() {
		SessionUpdateRequest request = new SessionUpdateRequest(fixedTime, new BigDecimal("300.00"), 1L, 2L);
		assertThatThrownBy(() -> mapper.updateEntity(null, request)).isInstanceOf(NullPointerException.class);
	}
}