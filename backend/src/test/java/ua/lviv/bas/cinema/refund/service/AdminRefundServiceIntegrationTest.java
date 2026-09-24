package ua.lviv.bas.cinema.refund.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import ua.lviv.bas.cinema.exception.core.InvalidSortPropertyException;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.movie.domain.enums.AgeRating;
import ua.lviv.bas.cinema.movie.domain.status.MovieStatus;
import ua.lviv.bas.cinema.movie.repository.MovieRepository;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.payment.repository.PaymentRepository;
import ua.lviv.bas.cinema.refund.domain.Refund;
import ua.lviv.bas.cinema.refund.domain.status.RefundStatus;
import ua.lviv.bas.cinema.refund.dto.response.AdminRefundListResponse;
import ua.lviv.bas.cinema.refund.repository.RefundRepository;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("ci")
@Import({TestcontainersConfig.class, NoOpCacheTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AdminRefundServiceIntegrationTest {

    private static final String EMAIL = "zzadminrefund@test.com";

    @Autowired
    private AdminRefundService adminRefundService;
    @Autowired
    private RefundRepository refundRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CinemaHallRepository cinemaHallRepository;
    @Autowired
    private UserRepository userRepository;

    private static User user;
    private static Booking booking;

    @BeforeEach
    void setUp() {
        if (user != null) {
            return;
        }
        user = userRepository.save(buildUser());
        var movie = movieRepository.save(buildMovie());
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("ZZREFUND Hall").build());
        var session = sessionRepository.save(Session.builder().movie(movie).hall(hall)
                .startTime(CinemaTime.now().plusDays(3)).basePrice(new BigDecimal("100.00")).build());
        booking = bookingRepository.save(Booking.builder().user(user).session(session)
                .status(BookingStatus.CONFIRMED).totalPrice(new BigDecimal("300.00"))
                .finalPrice(new BigDecimal("300.00")).expiresAt(Instant.now().plus(Duration.ofMinutes(20))).build());
        var payment = paymentRepository.save(Payment.builder().booking(booking).amount(new BigDecimal("300.00"))
                .status(PaymentStatus.PARTIALLY_REFUNDED).liqpayOrderId("ORD_ZZREFUND_1")
                .paymentTime(Instant.now()).build());

        refundRepository.save(buildRefund(payment, RefundStatus.PROCESSED, "100.00"));
        refundRepository.save(buildRefund(payment, RefundStatus.PROCESSING, "50.00"));
        refundRepository.save(buildRefund(payment, RefundStatus.REJECTED, "75.00"));
    }

    @Test
    void getRefundsShouldFilterByUserAndSortByAmount() {
        var page = adminRefundService.getRefunds(null, null, false, user.getId(), null, null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "totalAmount")));

        assertThat(page.getContent()).extracting(AdminRefundListResponse::totalAmount)
                .containsExactly(new BigDecimal("100.00"), new BigDecimal("75.00"), new BigDecimal("50.00"));
        var first = page.getContent().getFirst();
        assertThat(first.userEmail()).isEqualTo(EMAIL);
        assertThat(first.bookingId()).isEqualTo(booking.getId());
        assertThat(first.movieTitle()).isEqualTo("ZZREFUND Movie");
        assertThat(first.liqpayOrderId()).isEqualTo("ORD_ZZREFUND_1");
    }

    @Test
    void getRefundsShouldReturnOnlyRefundsNeedingAttention() {
        var page = adminRefundService.getRefunds(null, null, true, user.getId(), null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(AdminRefundListResponse::status)
                .containsExactlyInAnyOrder(RefundStatus.PROCESSING, RefundStatus.REJECTED);
    }

    @Test
    void getRefundsShouldSearchByBookingNumberAndOrderId() {
        var bookingNumber = String.format("BK-%d-%05d", CinemaTime.today().getYear(), booking.getId());

        var byNumber = adminRefundService.getRefunds(bookingNumber, RefundStatus.PROCESSED, false, null, null, null,
                PageRequest.of(0, 10));
        assertThat(byNumber.getContent()).singleElement()
                .extracting(AdminRefundListResponse::bookingNumber).isEqualTo(bookingNumber);

        var byOrder = adminRefundService.getRefunds("zzrefund_1", null, false, null, null, null,
                PageRequest.of(0, 10));
        assertThat(byOrder.getTotalElements()).isEqualTo(3);
    }

    @Test
    void getRefundsShouldFilterByCreationDate() {
        var page = adminRefundService.getRefunds(null, null, false, user.getId(), CinemaTime.today().plusDays(1),
                null, PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void getRefundsShouldRejectUnknownSortProperty() {
        assertThatThrownBy(() -> adminRefundService.getRefunds(null, null, false, null, null, null,
                PageRequest.of(0, 10, Sort.by("reason"))))
                .isInstanceOf(InvalidSortPropertyException.class);
    }

    private Refund buildRefund(Payment payment, RefundStatus status, String amount) {
        return Refund.builder().payment(payment).user(user).totalAmount(new BigDecimal(amount)).status(status)
                .reason("Cannot attend").build();
    }

    private User buildUser() {
        return User.builder().email(EMAIL).firstName("Admin").lastName("Refund")
                .dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv").phoneNumber("+380000000301")
                .password("hashed-password").userRole(UserRole.ROLE_USER).enabled(true).emailVerified(true).build();
    }

    private Movie buildMovie() {
        return Movie.builder().title("ZZREFUND Movie").slug("zzrefund-movie")
                .trailerUrl("https://example.com/trailer").description("Test movie for admin refunds")
                .durationMinutes(120).releaseDate(LocalDate.now().minusDays(1))
                .endShowingDate(LocalDate.now().plusMonths(1)).status(MovieStatus.CURRENT)
                .posterFileName("poster.jpg").ageRating(AgeRating.PEGI_12).build();
    }
}
