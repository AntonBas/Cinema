package ua.lviv.bas.cinema.payment.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.SeatReservation;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.booking.domain.status.ReservationStatus;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.booking.repository.SeatReservationRepository;
import ua.lviv.bas.cinema.booking.service.BookingService;
import ua.lviv.bas.cinema.bonus.domain.BonusCard;
import ua.lviv.bas.cinema.bonus.repository.BonusCardRepository;
import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.cinema.repository.SeatRepository;
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
import ua.lviv.bas.cinema.ticket.domain.TicketStatus;
import ua.lviv.bas.cinema.ticket.domain.TicketType;
import ua.lviv.bas.cinema.ticket.repository.TicketRepository;
import ua.lviv.bas.cinema.ticket.repository.TicketTypeRepository;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {"scheduler.booking.expiration-interval=3600000",
        "scheduler.payment.expiration-interval=3600000"})
@ActiveProfiles("ci")
@Import({TestcontainersConfig.class, NoOpCacheTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class LatePaymentRefundIntegrationTest {

    @MockitoBean
    private PaymentGatewayService paymentGatewayService;
    @MockitoBean
    private PaymentRefundService paymentRefundService;

    @Autowired
    private PaymentStatusService paymentStatusService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private TicketRepository ticketRepository;
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
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private SeatReservationRepository seatReservationRepository;
    @Autowired
    private TicketTypeRepository ticketTypeRepository;
    @Autowired
    private BonusCardRepository bonusCardRepository;

    private User user;
    private Session session;
    private TicketType ticketType;

    @BeforeEach
    void setUp() {
        var suffix = System.nanoTime();
        user = userRepository.save(User.builder().email("zztest.late.refund." + suffix + "@test.com")
                .firstName("Test").lastName("User").dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv")
                .phoneNumber("+38" + String.format("%010d", suffix % 10_000_000_000L)).password("hashed-password")
                .userRole(UserRole.ROLE_USER).enabled(true).emailVerified(true).build());
        bonusCardRepository.save(BonusCard.builder().user(user).pointsBalance(0).welcomeBonusReceived(true).build());
        var movie = movieRepository.save(Movie.builder().title("ZZTEST Late Refund Movie " + suffix)
                .slug("zztest-late-refund-movie-" + suffix).trailerUrl("https://example.com/trailer")
                .description("Test movie for late payment refunds").durationMinutes(120)
                .releaseDate(CinemaTime.today().minusDays(1)).endShowingDate(CinemaTime.today().plusMonths(1))
                .status(MovieStatus.CURRENT).posterFileName("poster.jpg").ageRating(AgeRating.PEGI_12).build());
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("ZZ Late " + suffix % 100_000).build());
        session = sessionRepository.save(Session.builder().movie(movie).hall(hall)
                .startTime(CinemaTime.now().plusDays(1)).basePrice(new BigDecimal("100.00")).build());
        ticketType = ticketTypeRepository.save(TicketType.builder().displayName("ZZTEST Late " + suffix).build());

        when(paymentGatewayService.checkRefundStatus(anyString(), any(), any()))
                .thenReturn(RefundGatewayStatus.NOT_CONFIRMED);
    }

    @Test
    void successCallbackForExpiredBookingShouldRefundThePaymentWithoutIssuingTickets() {
        var booking = saveBookingWithSeats(BookingStatus.EXPIRED, Instant.now().minus(Duration.ofMinutes(1)));
        var payment = savePendingPayment(booking, "ORD_ZZTEST_LATE_1");
        stubSuccessCallback("ORD_ZZTEST_LATE_1");

        paymentStatusService.handleCallback("data", "signature");

        assertThat(paymentRepository.findById(payment.getId()).orElseThrow().getStatus())
                .isEqualTo(PaymentStatus.REFUNDED);
        assertThat(ticketRepository.findByBookingId(booking.getId())).isEmpty();
        verify(paymentRefundService, times(1)).callLiqPayRefund(eq("LIQPAY_ORD_ZZTEST_LATE_1"),
                eq("ORD_ZZTEST_LATE_1"), any(), anyString());
    }

    @Test
    void cancellationRacingWithSuccessCallbackShouldEitherIssueTicketsOrRefundThePayment() throws Exception {
        var booking = saveBookingWithSeats(BookingStatus.PENDING, Instant.now().plus(Duration.ofMinutes(20)));
        var payment = savePendingPayment(booking, "ORD_ZZTEST_LATE_2");
        stubSuccessCallback("ORD_ZZTEST_LATE_2");

        var readyLatch = new CountDownLatch(2);
        var startLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<?> callback = executor.submit(() -> runAfterStart(readyLatch, startLatch,
                () -> paymentStatusService.handleCallback("data", "signature")));
        Future<?> cancellation = executor.submit(() -> runAfterStart(readyLatch, startLatch,
                () -> bookingService.cancelBooking(booking.getPublicId(), user)));

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();
        callback.get(20, TimeUnit.SECONDS);
        cancellation.get(20, TimeUnit.SECONDS);
        executor.shutdown();

        var finalBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        if (finalBooking.getStatus() == BookingStatus.CANCELLED
                && paymentRepository.findById(payment.getId()).orElseThrow().getStatus() == PaymentStatus.SUCCESS) {
            paymentService.refundUnfulfillableSuccess(payment.getId());
        }

        var finalPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        var activeTickets = ticketRepository.findByBookingId(booking.getId()).stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.ACTIVE).toList();

        if (finalBooking.getStatus() == BookingStatus.CONFIRMED) {
            assertThat(finalPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
            assertThat(activeTickets).hasSize(2);
        } else {
            assertThat(finalBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
            assertThat(finalPayment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            assertThat(activeTickets).isEmpty();
        }
    }

    private void runAfterStart(CountDownLatch readyLatch, CountDownLatch startLatch, Runnable action) {
        readyLatch.countDown();
        try {
            startLatch.await();
            action.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException ignored) {
        }
    }

    private void stubSuccessCallback(String orderId) {
        when(paymentGatewayService.processCallback(anyString(), anyString())).thenReturn(Map.of("order_id", orderId,
                "status", "success", "payment_id", "LIQPAY_" + orderId, "transaction_id", "TXN_" + orderId,
                "sender_card_mask", "****1234"));
    }

    private Booking saveBookingWithSeats(BookingStatus status, Instant expiresAt) {
        var booking = bookingRepository.save(Booking.builder().user(user).session(session).status(status)
                .totalPrice(new BigDecimal("200.00")).finalPrice(new BigDecimal("200.00")).expiresAt(expiresAt)
                .build());
        var reservationStatus = status == BookingStatus.PENDING ? ReservationStatus.PENDING
                : ReservationStatus.EXPIRED;
        for (int seatNumber = 1; seatNumber <= 2; seatNumber++) {
            var seat = seatRepository.save(Seat.builder().row(1).number(seatNumber).x(0).y(0)
                    .hall(session.getHall()).build());
            seatReservationRepository.save(SeatReservation.builder()
                    .booking(status == BookingStatus.PENDING ? booking : null).seat(seat).session(session)
                    .ticketType(ticketType).seatPrice(new BigDecimal("100.00")).status(reservationStatus)
                    .reservedUntil(Instant.now().plus(Duration.ofMinutes(5))).build());
        }
        return booking;
    }

    private Payment savePendingPayment(Booking booking, String orderId) {
        return paymentRepository.save(Payment.builder().booking(booking).amount(new BigDecimal("200.00"))
                .status(PaymentStatus.PENDING).liqpayOrderId(orderId).build());
    }
}
