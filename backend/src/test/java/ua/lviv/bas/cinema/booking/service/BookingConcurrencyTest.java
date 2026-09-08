package ua.lviv.bas.cinema.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.movie.domain.enums.AgeRating;
import ua.lviv.bas.cinema.movie.domain.status.MovieStatus;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.movie.repository.MovieRepository;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BookingConcurrencyTest {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CinemaHallRepository cinemaHallRepository;
    @Autowired
    private SessionRepository sessionRepository;

    private Long bookingId;

    @BeforeEach
    void setUp() {
        var user = userRepository.save(buildUser("booking.concurrency@test.com"));
        var movie = movieRepository.save(buildMovie());
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("Concurrency Lock Hall").build());
        var session = sessionRepository.save(Session.builder().movie(movie).hall(hall)
                .startTime(LocalDateTime.now().plusDays(1)).basePrice(new BigDecimal("100.00")).build());

        var booking = bookingRepository.save(Booking.builder().user(user).session(session)
                .status(BookingStatus.PENDING).totalPrice(new BigDecimal("100.00"))
                .finalPrice(new BigDecimal("100.00")).expiresAt(LocalDateTime.now().plusMinutes(20)).build());
        bookingId = booking.getId();
    }

    @Test
    void concurrentUpdatesToSameBookingOnlyOneShouldSucceed() throws Exception {
        var readyLatch = new CountDownLatch(2);
        var startLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Exception> updateToConfirmed = () -> attemptUpdate(readyLatch, startLatch, BookingStatus.CONFIRMED);
        Callable<Exception> updateToExpired = () -> attemptUpdate(readyLatch, startLatch, BookingStatus.EXPIRED);

        Future<Exception> resultA = executor.submit(updateToConfirmed);
        Future<Exception> resultB = executor.submit(updateToExpired);

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();

        Exception outcomeA = resultA.get(10, TimeUnit.SECONDS);
        Exception outcomeB = resultB.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        long successCount = Stream.of(outcomeA, outcomeB).filter(e -> e == null).count();
        long conflictCount = Stream.of(outcomeA, outcomeB)
                .filter(e -> e instanceof ObjectOptimisticLockingFailureException).count();

        assertThat(successCount).isEqualTo(1);
        assertThat(conflictCount).isEqualTo(1);

        var finalBooking = bookingRepository.findById(bookingId).orElseThrow();
        assertThat(finalBooking.getVersion()).isEqualTo(1L);
    }

    private Exception attemptUpdate(CountDownLatch readyLatch, CountDownLatch startLatch, BookingStatus status) {
        try {
            readyLatch.countDown();
            startLatch.await();
            var booking = bookingRepository.findById(bookingId).orElseThrow();
            booking.setStatus(status);
            bookingRepository.saveAndFlush(booking);
            return null;
        } catch (Exception e) {
            return e;
        }
    }

    private User buildUser(String email) {
        return User.builder().email(email).firstName("Test").lastName("User")
                .dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv").phoneNumber("+380000000010")
                .password("hashed-password").userRole(UserRole.ROLE_USER).enabled(true).build();
    }

    private Movie buildMovie() {
        return Movie.builder().title("Concurrency Lock Test Movie").slug("concurrency-lock-test-movie")
                .trailerUrl("https://example.com/trailer").description("Test movie for optimistic lock testing")
                .durationMinutes(120).releaseDate(LocalDate.now().minusDays(1))
                .endShowingDate(LocalDate.now().plusMonths(1)).status(MovieStatus.CURRENT)
                .posterFileName("poster.jpg").ageRating(AgeRating.PEGI_12).build();
    }
}
