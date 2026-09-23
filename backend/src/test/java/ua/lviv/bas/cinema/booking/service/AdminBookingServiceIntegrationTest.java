package ua.lviv.bas.cinema.booking.service;

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
import ua.lviv.bas.cinema.booking.dto.response.AdminBookingListResponse;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.common.CinemaTime;
import ua.lviv.bas.cinema.config.NoOpCacheTestConfig;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.core.InvalidSortPropertyException;
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

@SpringBootTest
@ActiveProfiles("ci")
@Import({TestcontainersConfig.class, NoOpCacheTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AdminBookingServiceIntegrationTest {

    private static final String EMAIL = "zzadminbooking@test.com";

    @Autowired
    private AdminBookingService adminBookingService;
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
    private static Session session;

    @BeforeEach
    void setUp() {
        if (user != null) {
            return;
        }
        user = userRepository.save(buildUser());
        var movie = movieRepository.save(buildMovie());
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("ZZADMIN Hall").build());
        session = sessionRepository.save(Session.builder().movie(movie).hall(hall)
                .startTime(CinemaTime.now().plusDays(2)).basePrice(new BigDecimal("100.00")).build());

        var paid = bookingRepository.save(buildBooking(BookingStatus.CONFIRMED, new BigDecimal("300.00")));
        paymentRepository.save(Payment.builder().booking(paid).amount(new BigDecimal("300.00"))
                .status(PaymentStatus.SUCCESS).liqpayOrderId("ORD_ZZADMIN_1").paymentTime(Instant.now()).build());
        bookingRepository.save(buildBooking(BookingStatus.PENDING, new BigDecimal("100.00")));
        bookingRepository.save(buildBooking(BookingStatus.EXPIRED, new BigDecimal("200.00")));
    }

    @Test
    void getBookingsShouldFilterByUserAndSortByPrice() {
        var page = adminBookingService.getBookings(null, null, null, null, user.getId(), null, null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "finalPrice")));

        assertThat(page.getContent()).extracting(AdminBookingListResponse::finalPrice)
                .containsExactly(new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("300.00"));
        assertThat(page.getContent().getFirst().movieTitle()).isEqualTo("ZZADMIN Movie");
        assertThat(page.getContent().getFirst().userEmail()).isEqualTo(EMAIL);
    }

    @Test
    void getBookingsShouldFilterByPaymentStatus() {
        var page = adminBookingService.getBookings(null, null, PaymentStatus.SUCCESS, session.getId(), null, null,
                null, PageRequest.of(0, 10));

        assertThat(page.getContent()).singleElement().satisfies(booking -> {
            assertThat(booking.status()).isEqualTo(BookingStatus.CONFIRMED);
            assertThat(booking.paymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        });
    }

    @Test
    void getBookingsShouldSearchByEmailAndBookingNumber() {
        var byEmail = adminBookingService.getBookings("zzadminbooking", BookingStatus.PENDING, null, null, null,
                null, null, PageRequest.of(0, 10));
        assertThat(byEmail.getContent()).singleElement()
                .extracting(AdminBookingListResponse::status).isEqualTo(BookingStatus.PENDING);

        var booking = byEmail.getContent().getFirst();
        var byNumber = adminBookingService.getBookings(booking.bookingNumber(), null, null, null, null, null, null,
                PageRequest.of(0, 10));
        assertThat(byNumber.getContent()).singleElement()
                .extracting(AdminBookingListResponse::id).isEqualTo(booking.id());
    }

    @Test
    void getBookingsShouldFilterByCreationDate() {
        var tomorrow = CinemaTime.today().plusDays(1);
        var page = adminBookingService.getBookings(null, null, null, null, user.getId(), tomorrow, null,
                PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void getBookingsShouldRejectUnknownSortProperty() {
        assertThatThrownBy(() -> adminBookingService.getBookings(null, null, null, null, null, null, null,
                PageRequest.of(0, 10, Sort.by("user.password"))))
                .isInstanceOf(InvalidSortPropertyException.class);
    }

    @Test
    void getBookingShouldReturnDetailsWithPayment() {
        var paid = adminBookingService.getBookings(null, BookingStatus.CONFIRMED, null, null, user.getId(), null,
                null, PageRequest.of(0, 10)).getContent().getFirst();

        var details = adminBookingService.getBooking(paid.id());

        assertThat(details.userEmail()).isEqualTo(EMAIL);
        assertThat(details.hallName()).isEqualTo("ZZADMIN Hall");
        assertThat(details.payment()).isNotNull();
        assertThat(details.payment().status()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(details.payment().liqpayOrderId()).isEqualTo("ORD_ZZADMIN_1");
        assertThat(details.tickets()).isEmpty();
    }

    @Test
    void getBookingShouldThrowWhenMissing() {
        assertThatThrownBy(() -> adminBookingService.getBooking(Long.MAX_VALUE))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private Booking buildBooking(BookingStatus status, BigDecimal price) {
        return Booking.builder().user(user).session(session).status(status).totalPrice(price).finalPrice(price)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(20))).build();
    }

    private User buildUser() {
        return User.builder().email(EMAIL).firstName("Admin").lastName("Booking")
                .dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv").phoneNumber("+380000000201")
                .password("hashed-password").userRole(UserRole.ROLE_USER).enabled(true).build();
    }

    private Movie buildMovie() {
        return Movie.builder().title("ZZADMIN Movie").slug("zzadmin-movie")
                .trailerUrl("https://example.com/trailer").description("Test movie for admin bookings")
                .durationMinutes(120).releaseDate(LocalDate.now().minusDays(1))
                .endShowingDate(LocalDate.now().plusMonths(1)).status(MovieStatus.CURRENT)
                .posterFileName("poster.jpg").ageRating(AgeRating.PEGI_12).build();
    }
}
