package ua.lviv.bas.cinema.mapper.cinema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.enums.AgeRating;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.session.request.SessionRequest;
import ua.lviv.bas.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionResponse;
import ua.lviv.bas.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.repository.cinema.projection.SessionAdminProjection;
import ua.lviv.bas.cinema.repository.cinema.projection.SessionScheduleProjection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SessionMapperTest {

    private final SessionMapper mapper = Mappers.getMapper(SessionMapper.class);
    private LocalDateTime fixedTime;

    @BeforeEach
    void setUp() {
        fixedTime = LocalDateTime.of(2026, 3, 18, 20, 0, 0);
    }

    @Test
    void toSessionResponse_ShouldMapAllFields() {
        Movie movie = Movie.builder().id(1L).title("Test Movie").durationMinutes(120).build();
        CinemaHall hall = CinemaHall.builder().id(1L).name("Hall 1").build();
        Session session = Session.builder().id(1L).startTime(fixedTime).basePrice(new BigDecimal("250.00")).movie(movie)
                .hall(hall).status(CinemaSessionStatus.SCHEDULED).build();

        SessionResponse response = mapper.toSessionResponse(session);

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
    }

    @Test
    void toSessionResponse_WithNullMovie_ShouldReturnNullEndTime() {
        CinemaHall hall = CinemaHall.builder().id(1L).name("Hall 1").build();
        Session session = Session.builder().id(1L).startTime(fixedTime).basePrice(new BigDecimal("250.00")).hall(hall)
                .status(CinemaSessionStatus.SCHEDULED).build();

        SessionResponse response = mapper.toSessionResponse(session);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.endTime()).isNull();
        assertThat(response.movieId()).isNull();
        assertThat(response.movieTitle()).isNull();
        assertThat(response.movieDuration()).isNull();
    }

    @Test
    void toSessionResponse_WithNullSession_ShouldReturnNull() {
        SessionResponse response = mapper.toSessionResponse(null);
        assertThat(response).isNull();
    }

    @Test
    void toSessionAdminResponseFromProjection_ShouldMapAllFields() {
        SessionAdminProjection projection = new SessionAdminProjection() {
            @Override
            public Long getId() {
                return 1L;
            }

            @Override
            public LocalDateTime getStartTime() {
                return fixedTime;
            }

            @Override
            public BigDecimal getBasePrice() {
                return new BigDecimal("250.00");
            }

            @Override
            public String getStatus() {
                return "SCHEDULED";
            }

            @Override
            public Long getMovieId() {
                return 1L;
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
                return 1L;
            }

            @Override
            public String getHallName() {
                return "Hall 1";
            }

            @Override
            public Long getHallCapacity() {
                return 150L;
            }

            @Override
            public Long getTicketsSold() {
                return 45L;
            }

            @Override
            public BigDecimal getTotalRevenue() {
                return new BigDecimal("6750.00");
            }
        };

        SessionAdminResponse response = mapper.toSessionAdminResponse(projection);

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
    void toSessionAdminResponse_WithNullProjection_ShouldReturnNull() {
        SessionAdminResponse response = mapper.toSessionAdminResponse(null);
        assertThat(response).isNull();
    }

    @Test
    void toSessionScheduleResponseFromProjection_ShouldMapAllFields() {
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

        SessionScheduleResponse response = mapper.toSessionScheduleResponse(projection);

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
    void toSessionScheduleResponse_WithNullProjection_ShouldReturnNull() {
        SessionScheduleResponse response = mapper.toSessionScheduleResponse(null);
        assertThat(response).isNull();
    }

    @Test
    void toEntity_ShouldMapFields() {
        LocalDateTime startTime = fixedTime.plusDays(1);
        SessionRequest request = new SessionRequest(startTime, new BigDecimal("300.00"), 1L, 2L);

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
    void toEntity_WithNullRequest_ShouldReturnNull() {
        Session session = mapper.toEntity(null);
        assertThat(session).isNull();
    }

    @Test
    void updateEntity_ShouldUpdateFields() {
        Session session = Session.builder().id(1L).startTime(fixedTime).basePrice(new BigDecimal("250.00"))
                .status(CinemaSessionStatus.SCHEDULED).build();

        LocalDateTime newStartTime = fixedTime.plusHours(1);
        SessionRequest request = new SessionRequest(newStartTime, new BigDecimal("300.00"), 2L, 3L);

        mapper.updateEntity(request, session);

        assertThat(session.getStartTime()).isEqualTo(newStartTime);
        assertThat(session.getBasePrice()).isEqualTo(new BigDecimal("300.00"));
        assertThat(session.getId()).isEqualTo(1L);
        assertThat(session.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
    }

    @Test
    void updateEntity_WithPartialUpdate_ShouldUpdateOnlyNonNullFields() {
        Session session = Session.builder().id(1L).startTime(fixedTime).basePrice(new BigDecimal("250.00"))
                .status(CinemaSessionStatus.SCHEDULED).build();

        SessionRequest request = new SessionRequest(null, new BigDecimal("300.00"), null, null);

        mapper.updateEntity(request, session);

        assertThat(session.getStartTime()).isEqualTo(fixedTime);
        assertThat(session.getBasePrice()).isEqualTo(new BigDecimal("300.00"));
        assertThat(session.getId()).isEqualTo(1L);
        assertThat(session.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
    }

    @Test
    void updateEntity_WithNullRequest_ShouldNotChange() {
        Session session = Session.builder().id(1L).startTime(fixedTime).basePrice(new BigDecimal("250.00"))
                .status(CinemaSessionStatus.SCHEDULED).build();

        mapper.updateEntity(null, session);

        assertThat(session.getStartTime()).isEqualTo(fixedTime);
        assertThat(session.getBasePrice()).isEqualTo(new BigDecimal("250.00"));
        assertThat(session.getId()).isEqualTo(1L);
        assertThat(session.getStatus()).isEqualTo(CinemaSessionStatus.SCHEDULED);
    }

    @Test
    void updateEntity_WithNullTarget_ShouldThrowException() {
        SessionRequest request = new SessionRequest(fixedTime, new BigDecimal("300.00"), 1L, 2L);
        assertThatThrownBy(() -> mapper.updateEntity(request, null)).isInstanceOf(NullPointerException.class);
    }
}