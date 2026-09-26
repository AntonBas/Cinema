package ua.lviv.bas.cinema.payment.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.common.CinemaTime;
import ua.lviv.bas.cinema.config.NoOpCacheTestConfig;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.movie.domain.enums.AgeRating;
import ua.lviv.bas.cinema.movie.domain.status.MovieStatus;
import ua.lviv.bas.cinema.movie.repository.MovieRepository;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.payment.repository.PaymentRepository;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {"scheduler.booking.expiration-interval=3600000",
        "scheduler.payment.expiration-interval=3600000"})
@ActiveProfiles("ci")
@Import({TestcontainersConfig.class, NoOpCacheTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PaymentStatusVersioningIntegrationTest {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private PaymentRepository paymentRepository;
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

    @Test
    void successShouldPersistGatewayDataAndBlockStaleOverwrite() {
        var booking = bookingRepository.save(buildBooking(userRepository.save(buildUser()), saveSession(),
                BookingStatus.PENDING, Instant.now().plus(Duration.ofMinutes(20))));
        var stale = paymentRepository.save(buildPayment(booking, PaymentStatus.PENDING,
                "ORD_ZZVER_" + System.nanoTime() % 1_000_000_000L));

        paymentService.processSuccess(stale, Map.of("payment_id", "LP_VERSION", "sender_card_mask", "4242**42"));

        var stored = paymentRepository.findById(stale.getId()).orElseThrow();
        assertThat(stored.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(stored.getLiqpayPaymentId()).isEqualTo("LP_VERSION");
        assertThat(stored.getVersion()).isGreaterThan(stale.getVersion());

        stale.setStatus(PaymentStatus.EXPIRED);
        assertThatThrownBy(() -> paymentRepository.save(stale))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
        assertThat(paymentRepository.findById(stale.getId()).orElseThrow().getStatus())
                .isEqualTo(PaymentStatus.SUCCESS);
    }

    private Session saveSession() {
        var suffix = System.nanoTime();
        var movie = movieRepository.save(Movie.builder().title("ZZTEST Expiry Movie " + suffix)
                .slug("zztest-expiry-movie-" + suffix).trailerUrl("https://example.com/trailer")
                .description("Test movie for payment expiry queries").durationMinutes(100)
                .releaseDate(CinemaTime.today().minusDays(1)).endShowingDate(CinemaTime.today().plusMonths(1))
                .status(MovieStatus.CURRENT).posterFileName("poster.jpg").ageRating(AgeRating.PEGI_12).build());
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("ZZ Exp " + suffix % 100_000).build());
        return sessionRepository.save(Session.builder().movie(movie).hall(hall)
                .startTime(CinemaTime.now().plusDays(1)).basePrice(new BigDecimal("100.00")).build());
    }

    private User buildUser() {
        var suffix = System.nanoTime();
        return User.builder().email("zztest.expiry." + suffix + "@test.com").firstName("Test").lastName("User")
                .dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv")
                .phoneNumber("+38" + String.format("%010d", suffix % 10_000_000_000L))
                .password("hashed-password").userRole(UserRole.ROLE_USER).enabled(true).emailVerified(true).build();
    }

    private Booking buildBooking(User user, Session session, BookingStatus status, Instant expiresAt) {
        return Booking.builder().user(user).session(session).status(status).totalPrice(new BigDecimal("100.00"))
                .finalPrice(new BigDecimal("100.00")).expiresAt(expiresAt).build();
    }

    private Payment buildPayment(Booking booking, PaymentStatus status, String orderId) {
        return Payment.builder().booking(booking).amount(new BigDecimal("100.00")).status(status)
                .liqpayOrderId(orderId).build();
    }
}
