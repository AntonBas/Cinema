package ua.lviv.bas.cinema.cinema.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.SeatReservation;
import ua.lviv.bas.cinema.booking.domain.status.ReservationStatus;
import ua.lviv.bas.cinema.booking.repository.SeatReservationRepository;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.booking.scheduler.BookingScheduler;
import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.domain.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.common.CinemaTime;
import ua.lviv.bas.cinema.config.NoOpCacheTestConfig;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionOperationException;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.movie.domain.enums.AgeRating;
import ua.lviv.bas.cinema.movie.domain.status.MovieStatus;
import ua.lviv.bas.cinema.movie.repository.MovieRepository;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.payment.repository.PaymentRepository;
import ua.lviv.bas.cinema.refund.domain.status.RefundStatus;
import ua.lviv.bas.cinema.refund.repository.RefundRepository;
import ua.lviv.bas.cinema.refund.scheduler.CancelledSessionRefundScheduler;
import ua.lviv.bas.cinema.ticket.domain.Ticket;
import ua.lviv.bas.cinema.ticket.domain.TicketStatus;
import ua.lviv.bas.cinema.ticket.domain.TicketType;
import ua.lviv.bas.cinema.ticket.repository.TicketRepository;
import ua.lviv.bas.cinema.ticket.repository.TicketTypeRepository;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("ci")
@Import({TestcontainersConfig.class, NoOpCacheTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SessionCancellationIntegrationTest {

    private static final BigDecimal PRICE = new BigDecimal("100.00");

    @Autowired
    private SessionService sessionService;
    @Autowired
    private BookingScheduler bookingScheduler;
    @Autowired
    private CancelledSessionRefundScheduler cancelledSessionRefundScheduler;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private TicketTypeRepository ticketTypeRepository;
    @Autowired
    private RefundRepository refundRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CinemaHallRepository cinemaHallRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private SeatReservationRepository seatReservationRepository;

    @Test
    void cancellingSessionShouldCancelPendingBookingsAndRefundSoldTicketsInFull() {
        var suffix = System.nanoTime();
        var session = saveSession(suffix, CinemaTime.now().plusMinutes(90));
        var buyer = userRepository.save(buildUser(suffix, "buyer"));
        var waiter = userRepository.save(buildUser(suffix, "waiter"));

        var confirmed = bookingRepository.save(buildBooking(buyer, session, BookingStatus.CONFIRMED));
        var pending = bookingRepository.save(buildBooking(waiter, session, BookingStatus.PENDING));
        var payment = paymentRepository.save(Payment.builder().booking(confirmed).amount(PRICE)
                .status(PaymentStatus.SUCCESS).liqpayOrderId("ORD_CANCEL_" + suffix).liqpayPaymentId("PAY_" + suffix)
                .build());
        var ticketType = ticketTypeRepository.save(TicketType.builder().displayName("Cancel " + suffix).build());
        var seat = seatRepository.save(Seat.builder().hall(session.getHall()).row(1).number(1).x(0).y(0).build());
        var reservation = seatReservationRepository.save(SeatReservation.builder().booking(confirmed).seat(seat)
                .session(session).ticketType(ticketType).seatPrice(PRICE).status(ReservationStatus.CONFIRMED)
                .reservedUntil(Instant.now().plus(Duration.ofMinutes(20))).reservedByUser(buyer).build());
        var ticket = ticketRepository.save(Ticket.builder().booking(confirmed).user(buyer).ticketType(ticketType)
                .seatReservation(reservation)
                .payment(payment).purchaseTime(Instant.now().minus(Duration.ofHours(1))).originalPrice(PRICE)
                .finalPrice(PRICE).uniqueCode("TC" + suffix % 1_000_000_000L).status(TicketStatus.ACTIVE).build());

        sessionService.cancelSession(session.getId());
        bookingScheduler.cancelPendingBookingsOfCancelledSessions();
        cancelledSessionRefundScheduler.refundTicketsOfCancelledSessions();

        assertThat(sessionRepository.findById(session.getId()).orElseThrow().getStatus())
                .isEqualTo(CinemaSessionStatus.CANCELLED);
        assertThat(bookingRepository.findById(pending.getId()).orElseThrow().getStatus())
                .isEqualTo(BookingStatus.CANCELLED);
        assertThat(ticketRepository.findById(ticket.getId()).orElseThrow().getStatus())
                .isEqualTo(TicketStatus.REFUNDED);
        assertThat(paymentRepository.findById(payment.getId()).orElseThrow().getStatus())
                .isEqualTo(PaymentStatus.REFUNDED);
        var refund = refundRepository.findByUserIdOrderByCreatedDateDesc(buyer.getId()).getFirst();
        assertThat(refund.getStatus()).isEqualTo(RefundStatus.PROCESSED);
        assertThat(refund.getTotalAmount()).isEqualByComparingTo(PRICE);
    }

    @Test
    void deletingSessionWithBookingsShouldBeRejected() {
        var suffix = System.nanoTime();
        var session = saveSession(suffix, CinemaTime.now().plusDays(2));
        bookingRepository.save(buildBooking(userRepository.save(buildUser(suffix, "del")), session,
                BookingStatus.EXPIRED));

        assertThatThrownBy(() -> sessionService.deleteSession(session.getId()))
                .isInstanceOf(SessionOperationException.class);
        assertThat(sessionRepository.existsById(session.getId())).isTrue();
    }

    private Session saveSession(long suffix, LocalDateTime startTime) {
        var movie = movieRepository.save(Movie.builder().title("ZZTEST Cancel Movie " + suffix)
                .slug("zztest-cancel-movie-" + suffix).trailerUrl("https://example.com/trailer")
                .description("Test movie for session cancellation").durationMinutes(100)
                .releaseDate(LocalDate.now().minusDays(1)).endShowingDate(LocalDate.now().plusMonths(1))
                .status(MovieStatus.CURRENT).posterFileName("poster.jpg").ageRating(AgeRating.PEGI_12).build());
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("ZZ Cancel " + suffix % 100_000).build());
        return sessionRepository.save(Session.builder().movie(movie).hall(hall).startTime(startTime)
                .basePrice(PRICE).build());
    }

    private User buildUser(long suffix, String role) {
        return User.builder().email("zztest.cancel." + role + "." + suffix + "@test.com").firstName("Test")
                .lastName("User").dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv")
                .phoneNumber("+38" + String.format("%010d", (suffix + role.hashCode()) % 10_000_000_000L))
                .password("hashed-password").userRole(UserRole.ROLE_USER).enabled(true).emailVerified(true).build();
    }

    private Booking buildBooking(User user, Session session, BookingStatus status) {
        return Booking.builder().user(user).session(session).status(status).totalPrice(PRICE).finalPrice(PRICE)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(20))).build();
    }
}
