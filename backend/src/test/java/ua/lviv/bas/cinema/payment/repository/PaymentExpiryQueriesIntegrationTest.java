package ua.lviv.bas.cinema.payment.repository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"scheduler.booking.expiration-interval=3600000",
        "scheduler.payment.expiration-interval=3600000"})
@ActiveProfiles("ci")
@Import({TestcontainersConfig.class, NoOpCacheTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PaymentExpiryQueriesIntegrationTest {

    private static final List<PaymentStatus> ACTIVE_STATUSES = List.of(PaymentStatus.PENDING,
            PaymentStatus.PROCESSING);

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
    void expiredBookingWithActivePaymentShouldBeLeftToPaymentSchedulerOnly() {
        var session = saveSession();
        var user = userRepository.save(buildUser());
        var now = Instant.now();

        var withPayment = bookingRepository.save(buildBooking(user, session, BookingStatus.PENDING,
                now.minus(Duration.ofMinutes(1))));
        var withoutPayment = bookingRepository.save(buildBooking(user, session, BookingStatus.PENDING,
                now.minus(Duration.ofMinutes(1))));
        var payment = paymentRepository.save(buildPayment(withPayment, PaymentStatus.PENDING, "ORD_ZZTEST_EXP_1"));

        var bookingIds = bookingRepository.findExpiredWithoutActivePayment(BookingStatus.PENDING, now,
                ACTIVE_STATUSES).stream().map(Booking::getId).toList();
        var paymentIds = paymentRepository.findByStatusInAndBookingStatusInAndBookingExpiredBefore(ACTIVE_STATUSES,
                List.of(BookingStatus.PENDING), now).stream().map(Payment::getId).toList();

        assertThat(bookingIds).contains(withoutPayment.getId()).doesNotContain(withPayment.getId());
        assertThat(paymentIds).contains(payment.getId());
    }

    @Test
    void activePaymentOfCancelledBookingShouldBeFoundOnlyAfterBookingWindowEnds() {
        var session = saveSession();
        var user = userRepository.save(buildUser());
        var now = Instant.now();

        var windowEnded = bookingRepository.save(buildBooking(user, session, BookingStatus.CANCELLED,
                now.minus(Duration.ofMinutes(1))));
        var windowOpen = bookingRepository.save(buildBooking(user, session, BookingStatus.CANCELLED,
                now.plus(Duration.ofMinutes(10))));
        var abandoned = paymentRepository.save(buildPayment(windowEnded, PaymentStatus.PENDING, "ORD_ZZTEST_EXP_4"));
        var stillPayable = paymentRepository.save(buildPayment(windowOpen, PaymentStatus.PENDING, "ORD_ZZTEST_EXP_5"));

        var ids = paymentRepository.findByStatusInAndBookingStatusInAndBookingExpiredBefore(ACTIVE_STATUSES,
                List.of(BookingStatus.PENDING, BookingStatus.EXPIRED, BookingStatus.CANCELLED), now).stream()
                .map(Payment::getId).toList();

        assertThat(ids).contains(abandoned.getId()).doesNotContain(stillPayable.getId());
    }

    @Test
    void successfulPaymentOfExpiredBookingWithoutTicketsShouldBeFoundForRefund() {
        var session = saveSession();
        var user = userRepository.save(buildUser());

        var expired = bookingRepository.save(buildBooking(user, session, BookingStatus.EXPIRED,
                Instant.now().minus(Duration.ofHours(1))));
        var confirmed = bookingRepository.save(buildBooking(user, session, BookingStatus.CONFIRMED,
                Instant.now().minus(Duration.ofHours(1))));
        var orphaned = paymentRepository.save(buildPayment(expired, PaymentStatus.SUCCESS, "ORD_ZZTEST_EXP_2"));
        var fulfilled = paymentRepository.save(buildPayment(confirmed, PaymentStatus.SUCCESS, "ORD_ZZTEST_EXP_3"));

        var ids = paymentRepository.findWithoutTicketsByStatusAndBookingStatusIn(PaymentStatus.SUCCESS,
                List.of(BookingStatus.EXPIRED, BookingStatus.CANCELLED), Instant.now().plusSeconds(60)).stream()
                .map(Payment::getId).toList();

        assertThat(ids).contains(orphaned.getId()).doesNotContain(fulfilled.getId());
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
