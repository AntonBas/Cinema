package ua.lviv.bas.cinema.booking.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ua.lviv.bas.cinema.cinema.hall.CinemaHall;
import ua.lviv.bas.cinema.movie.Movie;
import ua.lviv.bas.cinema.cinema.hall.Seat;
import ua.lviv.bas.cinema.cinema.Session;
import ua.lviv.bas.cinema.movie.AgeRating;
import ua.lviv.bas.cinema.movie.MovieStatus;
import ua.lviv.bas.cinema.user.User;
import ua.lviv.bas.cinema.user.UserRole;
import ua.lviv.bas.cinema.booking.exception.SeatNotAvailableException;
import ua.lviv.bas.cinema.cinema.hall.CinemaHallRepository;
import ua.lviv.bas.cinema.movie.MovieRepository;
import ua.lviv.bas.cinema.cinema.hall.SeatRepository;
import ua.lviv.bas.cinema.cinema.SessionRepository;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.user.UserRepository;
import ua.lviv.bas.cinema.common.CinemaTime;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
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
        var suffix = UUID.randomUUID().toString().substring(0, 8);
        userA = userRepository.save(buildUser("concurrency.a." + suffix + "@test.com"));
        userB = userRepository.save(buildUser("concurrency.b." + suffix + "@test.com"));

        var movie = movieRepository.save(buildMovie(suffix));
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("Concurrency Hall " + suffix).build());
        var seat = seatRepository.save(Seat.builder().row(1).number(1).x(0).y(0).hall(hall).build());
        var session = sessionRepository.save(Session.builder().movie(movie).hall(hall)
                .startTime(CinemaTime.now().plusDays(1)).basePrice(new BigDecimal("100.00")).build());

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

        var reservations = seatReservationRepository.findBySessionIdAndSeatId(sessionId, seatId);
        assertThat(reservations).hasSize(1);
        assertThat(reservations.getFirst().getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(reservations.getFirst().getSeat().getId()).isEqualTo(seatId);
    }

    @Test
    void holdShouldSucceedWhenPreviousHoldExpiredButSchedulerHasNotRunYet() {
        var seat = seatRepository.findById(seatId).orElseThrow();
        var session = sessionRepository.findById(sessionId).orElseThrow();
        seatReservationRepository.save(SeatReservation.builder().seat(seat).session(session)
                .status(ReservationStatus.PENDING).reservedUntil(Instant.now().minusSeconds(60))
                .reservedByUser(userA).build());

        var availability = seatReservationService.getAvailability(sessionId);
        assertThat(availability.seats()).singleElement().satisfies(info -> assertThat(info.available()).isTrue());

        seatReservationService.hold(sessionId, seatId, userB);

        var reservations = seatReservationRepository.findBySessionIdAndSeatId(sessionId, seatId);
        assertThat(reservations).hasSize(1);
        assertThat(reservations.getFirst().getReservedByUser().getId()).isEqualTo(userB.getId());
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
                .password("hashed-password").userRole(UserRole.ROLE_USER).enabled(true).emailVerified(true).build();
    }

    private Movie buildMovie(String suffix) {
        return Movie.builder().title("Concurrency Test Movie " + suffix).slug("concurrency-test-movie-" + suffix)
                .trailerUrl("https://example.com/trailer").description("Test movie for concurrency testing")
                .durationMinutes(120).releaseDate(CinemaTime.today().minusDays(1))
                .endShowingDate(CinemaTime.today().plusMonths(1)).status(MovieStatus.CURRENT)
                .posterFileName("poster.jpg").ageRating(AgeRating.PEGI_12).build();
    }
}
