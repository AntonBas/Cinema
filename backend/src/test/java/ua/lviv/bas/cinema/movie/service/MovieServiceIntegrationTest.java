package ua.lviv.bas.cinema.movie.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.domain.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.cinema.dto.session.response.SessionMovieInfoResponse;
import ua.lviv.bas.cinema.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.config.NoOpCacheTestConfig;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.movie.domain.enums.AgeRating;
import ua.lviv.bas.cinema.movie.domain.status.MovieStatus;
import ua.lviv.bas.cinema.movie.repository.MovieRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("ci")
@Import({TestcontainersConfig.class, NoOpCacheTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MovieServiceIntegrationTest {

    @Autowired
    private MovieService movieService;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CinemaHallRepository cinemaHallRepository;
    @Autowired
    private SessionRepository sessionRepository;

    @Test
    void getMovieBySlugShouldReturnOnlyUpcomingScheduledSessionsInStartOrder() {
        var movie = movieRepository.save(buildMovie());
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("ZZTEST Showtimes Hall").build());
        var now = LocalDateTime.now().withNano(0);

        var later = saveSession(movie, hall, now.plusDays(3), CinemaSessionStatus.SCHEDULED);
        var sooner = saveSession(movie, hall, now.plusDays(1), CinemaSessionStatus.SCHEDULED);
        saveSession(movie, hall, now.plusDays(2), CinemaSessionStatus.CANCELLED);
        saveSession(movie, hall, now.minusDays(1), CinemaSessionStatus.COMPLETED);
        saveSession(movie, hall, now.minusHours(1), CinemaSessionStatus.SCHEDULED);

        var detail = movieService.getMovieBySlug(movie.getSlug());

        assertThat(detail.sessions()).extracting(SessionMovieInfoResponse::id)
                .containsExactly(sooner.getId(), later.getId());
    }

    private Session saveSession(Movie movie, CinemaHall hall, LocalDateTime startTime, CinemaSessionStatus status) {
        return sessionRepository.save(Session.builder().movie(movie).hall(hall).startTime(startTime)
                .basePrice(new BigDecimal("150.00")).status(status).build());
    }

    private Movie buildMovie() {
        return Movie.builder().title("ZZTEST Showtimes Movie").slug("zztest-showtimes-movie")
                .trailerUrl("https://example.com/trailer").description("Test movie for showtimes filtering")
                .durationMinutes(120).releaseDate(LocalDate.now().minusDays(7))
                .endShowingDate(LocalDate.now().plusMonths(1)).status(MovieStatus.CURRENT)
                .posterFileName("poster.jpg").ageRating(AgeRating.PEGI_12).build();
    }
}
