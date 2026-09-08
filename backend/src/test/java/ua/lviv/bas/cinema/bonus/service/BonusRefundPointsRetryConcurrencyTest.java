package ua.lviv.bas.cinema.bonus.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ua.lviv.bas.cinema.bonus.domain.BonusCard;
import ua.lviv.bas.cinema.bonus.repository.BonusCardRepository;
import ua.lviv.bas.cinema.bonus.repository.BonusTransactionRepository;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.movie.domain.enums.AgeRating;
import ua.lviv.bas.cinema.movie.domain.status.MovieStatus;
import ua.lviv.bas.cinema.movie.repository.MovieRepository;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@ActiveProfiles("ci")
@Import(TestcontainersConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BonusRefundPointsRetryConcurrencyTest {

    private static final int BONUS_POINTS_USED = 50;

    @Autowired
    private BonusLedgerService bonusLedgerService;
    @Autowired
    private BonusCardRepository bonusCardRepository;
    @Autowired
    private BonusTransactionRepository bonusTransactionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CinemaHallRepository cinemaHallRepository;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private BookingRepository bookingRepository;

    private Booking booking;

    @BeforeEach
    void setUp() {
        var user = userRepository.save(buildUser("bonus.refund.retry." + UUID.randomUUID() + "@test.com"));
        bonusCardRepository.save(BonusCard.builder().user(user).pointsBalance(0).welcomeBonusReceived(true).build());

        var movie = movieRepository.save(buildMovie());
        var hallSuffix = UUID.randomUUID().toString().substring(0, 8);
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("Hall " + hallSuffix).build());
        var session = sessionRepository.save(Session.builder().movie(movie).hall(hall)
                .startTime(LocalDateTime.now().plusDays(1)).basePrice(new BigDecimal("100.00")).build());

        booking = bookingRepository.save(Booking.builder().user(user).session(session)
                .status(BookingStatus.PENDING).totalPrice(new BigDecimal("100.00"))
                .finalPrice(new BigDecimal("100.00")).bonusPointsUsed(BONUS_POINTS_USED)
                .expiresAt(LocalDateTime.now().plusMinutes(20)).build());
    }

    @Test
    void refundPointsRetriedAfterOuterTransactionRollbackShouldNotFailOrDoubleCredit() {
        bonusLedgerService.refundPoints(booking);

        assertThatCode(() -> bonusLedgerService.refundPoints(booking)).doesNotThrowAnyException();

        var card = bonusCardRepository.findByUserId(booking.getUser().getId()).orElseThrow();
        assertThat(card.getPointsBalance()).isEqualTo(BONUS_POINTS_USED);
        assertThat(bonusTransactionRepository.existsByReferenceId("REFUND_BOOKING_" + booking.getId())).isTrue();
    }

    @Test
    void concurrentRefundPointsForSameBookingShouldApplyOnlyOnce() throws Exception {
        var readyLatch = new CountDownLatch(2);
        var startLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Exception> refundA = () -> attemptRefund(readyLatch, startLatch);
        Callable<Exception> refundB = () -> attemptRefund(readyLatch, startLatch);

        Future<Exception> resultA = executor.submit(refundA);
        Future<Exception> resultB = executor.submit(refundB);

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();

        Exception outcomeA = resultA.get(10, TimeUnit.SECONDS);
        Exception outcomeB = resultB.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(outcomeA).isNull();
        assertThat(outcomeB).isNull();

        var card = bonusCardRepository.findByUserId(booking.getUser().getId()).orElseThrow();
        assertThat(card.getPointsBalance()).isEqualTo(BONUS_POINTS_USED);
    }

    private Exception attemptRefund(CountDownLatch readyLatch, CountDownLatch startLatch) {
        try {
            readyLatch.countDown();
            startLatch.await();
            bonusLedgerService.refundPoints(booking);
            return null;
        } catch (Exception e) {
            return e;
        }
    }

    private User buildUser(String email) {
        return User.builder().email(email).firstName("Test").lastName("User")
                .dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv").phoneNumber("+380000000012")
                .password("hashed-password").userRole(UserRole.ROLE_USER).enabled(true).build();
    }

    private Movie buildMovie() {
        var uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        return Movie.builder().title("Refund Retry " + uniqueSuffix).slug("refund-retry-" + uniqueSuffix)
                .trailerUrl("https://example.com/trailer").description("Test movie for bonus refund retry testing")
                .durationMinutes(120).releaseDate(LocalDate.now().minusDays(1))
                .endShowingDate(LocalDate.now().plusMonths(1)).status(MovieStatus.CURRENT)
                .posterFileName("poster.jpg").ageRating(AgeRating.PEGI_12).build();
    }
}
