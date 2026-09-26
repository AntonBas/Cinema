package ua.lviv.bas.cinema.booking.repository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.config.NoOpCacheTestConfig;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.movie.domain.enums.AgeRating;
import ua.lviv.bas.cinema.movie.domain.status.MovieStatus;
import ua.lviv.bas.cinema.movie.repository.MovieRepository;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.repository.UserRepository;
import ua.lviv.bas.cinema.common.CinemaTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.datasource.hikari.connection-init-sql=SET TIME ZONE 'America/New_York'",
        "scheduler.booking.expiration-interval=3600000"})
@ActiveProfiles("ci")
@Import({TestcontainersConfig.class, NoOpCacheTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BookingMomentPersistenceIntegrationTest {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CinemaHallRepository cinemaHallRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void expiresAtShouldKeepTheSameInstantAndCompareCorrectlyRegardlessOfDatabaseSessionTimeZone() {
        var session = saveSession();
        var user = userRepository.save(buildUser());
        var now = Instant.now().truncatedTo(ChronoUnit.MICROS);

        var expired = bookingRepository.save(buildBooking(user, session, now.minus(Duration.ofMinutes(1))));
        var active = bookingRepository.save(buildBooking(user, session, now.plus(Duration.ofMinutes(10))));

        var expiredIds = bookingRepository.findExpiredWithoutActivePayment(BookingStatus.PENDING, now,
                List.of(PaymentStatus.PENDING, PaymentStatus.PROCESSING)).stream().map(Booking::getId).toList();
        assertThat(expiredIds).contains(expired.getId()).doesNotContain(active.getId());

        assertThat(bookingRepository.findById(active.getId()).orElseThrow().getExpiresAt())
                .isEqualTo(now.plus(Duration.ofMinutes(10)));

        Double storedEpochSeconds = jdbcTemplate.queryForObject(
                "SELECT EXTRACT(EPOCH FROM expires_at) FROM bookings WHERE id = ?", Double.class, active.getId());
        assertThat(storedEpochSeconds).isNotNull();
        assertThat(storedEpochSeconds.longValue()).isEqualTo(now.plus(Duration.ofMinutes(10)).getEpochSecond());
    }

    private Session saveSession() {
        var movie = movieRepository.save(Movie.builder().title("ZZTEST Moment Movie").slug("zztest-moment-movie")
                .trailerUrl("https://example.com/trailer").description("Test movie for moment persistence")
                .durationMinutes(100).releaseDate(CinemaTime.today().minusDays(1))
                .endShowingDate(CinemaTime.today().plusMonths(1)).status(MovieStatus.CURRENT)
                .posterFileName("poster.jpg").ageRating(AgeRating.PEGI_12).build());
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("ZZTEST Moment Hall").build());
        return sessionRepository.save(Session.builder().movie(movie).hall(hall)
                .startTime(CinemaTime.now().plusDays(1)).basePrice(new BigDecimal("100.00")).build());
    }

    private User buildUser() {
        return User.builder().email("zztest.moment@test.com").firstName("Test").lastName("User")
                .dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv").phoneNumber("+380000000016")
                .password("hashed-password").userRole(UserRole.ROLE_USER).enabled(true).emailVerified(true).build();
    }

    private Booking buildBooking(User user, Session session, Instant expiresAt) {
        return Booking.builder().user(user).session(session).status(BookingStatus.PENDING)
                .totalPrice(new BigDecimal("100.00")).finalPrice(new BigDecimal("100.00")).expiresAt(expiresAt)
                .build();
    }
}
