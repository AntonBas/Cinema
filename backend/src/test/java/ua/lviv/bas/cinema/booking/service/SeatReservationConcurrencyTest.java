package ua.lviv.bas.cinema.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ua.lviv.bas.cinema.booking.domain.status.ReservationStatus;
import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.movie.domain.enums.AgeRating;
import ua.lviv.bas.cinema.movie.domain.status.MovieStatus;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.exception.domain.booking.SeatNotAvailableException;
import ua.lviv.bas.cinema.booking.repository.SeatReservationRepository;
import ua.lviv.bas.cinema.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.movie.repository.MovieRepository;
import ua.lviv.bas.cinema.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("ci")
@Import(TestcontainersConfig.class)
class SeatReservationConcurrencyTest {

    @Autowired
    private SeatReservationService seatReservationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CinemaHallRepository cinemaHallRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private SeatReservationRepository seatReservationRepository;

    private Long sessionId;
    private Long seatId;
    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        userA = userRepository.save(buildUser("concurrency.a@test.com"));
        userB = userRepository.save(buildUser("concurrency.b@test.com"));

        var movie = movieRepository.save(buildMovie());
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("Concurrency Hall").build());
        var seat = seatRepository.save(Seat.builder().row(1).number(1).hall(hall).build());
        var session = sessionRepository.save(Session.builder().movie(movie).hall(hall)
                .startTime(LocalDateTime.now().plusDays(1)).basePrice(new BigDecimal("100.00")).build());

        seatId = seat.getId();
        sessionId = session.getId();
    }

    @Test
    void holdWhenTwoUsersRaceForSameSeatOnlyOneShouldSucceed() throws Exception {
        var readyLatch = new CountDownLatch(2);
        var startLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Exception> holdByUserA = () -> attemptHold(readyLatch, startLatch, userA);
        Callable<Exception> holdByUserB = () -> attemptHold(readyLatch, startLatch, userB);

        Future<Exception> resultA = executor.submit(holdByUserA);
        Future<Exception> resultB = executor.submit(holdByUserB);

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();

        Exception outcomeA = resultA.get(10, TimeUnit.SECONDS);
        Exception outcomeB = resultB.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        long successCount = Stream.of(outcomeA, outcomeB).filter(e -> e == null).count();
        long rejectedCount = Stream.of(outcomeA, outcomeB).filter(e -> e instanceof SeatNotAvailableException).count();

        assertThat(successCount).isEqualTo(1);
        assertThat(rejectedCount).isEqualTo(1);

        var reservations = seatReservationRepository.findAll();
        assertThat(reservations).hasSize(1);
        assertThat(reservations.getFirst().getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(reservations.getFirst().getSeat().getId()).isEqualTo(seatId);
    }

    private Exception attemptHold(CountDownLatch readyLatch, CountDownLatch startLatch, User user) {
        try {
            readyLatch.countDown();
            startLatch.await();
            seatReservationService.hold(sessionId, seatId, user);
            return null;
        } catch (Exception e) {
            return e;
        }
    }

    private User buildUser(String email) {
        return User.builder().email(email).firstName("Test").lastName("User")
                .dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv").phoneNumber("+380000000000")
                .password("hashed-password").userRole(UserRole.ROLE_USER).enabled(true).build();
    }

    private Movie buildMovie() {
        return Movie.builder().title("Concurrency Test Movie").slug("concurrency-test-movie")
                .trailerUrl("https://example.com/trailer").description("Test movie for concurrency testing")
                .durationMinutes(120).releaseDate(LocalDate.now().minusDays(1))
                .endShowingDate(LocalDate.now().plusMonths(1)).status(MovieStatus.CURRENT)
                .posterFileName("poster.jpg").ageRating(AgeRating.PEGI_12).build();
    }
}
