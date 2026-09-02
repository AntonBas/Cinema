package ua.lviv.bas.cinema.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.SeatReservation;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.booking.domain.status.ReservationStatus;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.booking.repository.SeatReservationRepository;
import ua.lviv.bas.cinema.bonus.domain.BonusCard;
import ua.lviv.bas.cinema.bonus.repository.BonusCardRepository;
import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.movie.domain.enums.AgeRating;
import ua.lviv.bas.cinema.movie.domain.status.MovieStatus;
import ua.lviv.bas.cinema.movie.repository.MovieRepository;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.payment.repository.PaymentRepository;
import ua.lviv.bas.cinema.ticket.domain.TicketType;
import ua.lviv.bas.cinema.ticket.repository.TicketRepository;
import ua.lviv.bas.cinema.ticket.repository.TicketTypeRepository;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Objects;
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
class PaymentCallbackConcurrencyTest {

    private static final String PRIVATE_KEY = "test_private_key";
    private static final String ORDER_ID = "ORD_CALLBACK_CONCURRENCY";

    @Autowired
    private PaymentStatusService paymentStatusService;
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

    private Long bookingId;

    @BeforeEach
    void setUp() {
        var user = userRepository.save(buildUser("payment.callback.concurrency@test.com"));
        bonusCardRepository.save(BonusCard.builder().user(user).pointsBalance(0).welcomeBonusReceived(true).build());
        var movie = movieRepository.save(buildMovie());
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("Callback Concurrency Hall").build());
        var session = sessionRepository.save(Session.builder().movie(movie).hall(hall)
                .startTime(LocalDateTime.now().plusDays(1)).basePrice(new BigDecimal("100.00")).build());
        var ticketType = ticketTypeRepository.save(TicketType.builder().displayName("Standard").build());

        var booking = bookingRepository.save(Booking.builder().user(user).session(session)
                .status(BookingStatus.PENDING).totalPrice(new BigDecimal("200.00"))
                .finalPrice(new BigDecimal("200.00")).expiresAt(LocalDateTime.now().plusMinutes(20)).build());
        bookingId = booking.getId();

        for (int seatNumber = 1; seatNumber <= 2; seatNumber++) {
            var seat = seatRepository.save(Seat.builder().row(1).number(seatNumber).hall(hall).build());
            seatReservationRepository.save(SeatReservation.builder().booking(booking).seat(seat).session(session)
                    .ticketType(ticketType).seatPrice(new BigDecimal("100.00")).status(ReservationStatus.PENDING)
                    .reservedUntil(LocalDateTime.now().plusMinutes(5)).build());
        }

        paymentRepository.save(Payment.builder().booking(booking).amount(new BigDecimal("200.00"))
                .status(PaymentStatus.PENDING).liqpayOrderId(ORDER_ID).build());
    }

    @Test
    void concurrentSuccessCallbacksShouldCreateTicketsOnlyOnce() throws Exception {
        var callbackData = new LinkedHashMap<String, String>();
        callbackData.put("order_id", ORDER_ID);
        callbackData.put("status", "success");
        callbackData.put("payment_id", "LIQPAY_PAY_1");
        callbackData.put("transaction_id", "TXN_1");
        callbackData.put("sender_card_mask", "****1234");
        var data = LiqPayDecoder.encodeToBase64(callbackData);
        var signature = LiqPayDecoder.generateSignature(data, PRIVATE_KEY);

        var readyLatch = new CountDownLatch(2);
        var startLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Exception> callback = () -> attemptCallback(readyLatch, startLatch, data, signature);

        Future<Exception> resultA = executor.submit(callback);
        Future<Exception> resultB = executor.submit(callback);

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();

        Exception outcomeA = resultA.get(20, TimeUnit.SECONDS);
        Exception outcomeB = resultB.get(20, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(Stream.of(outcomeA, outcomeB).filter(Objects::nonNull).toList()).isEmpty();

        var tickets = ticketRepository.findByBookingId(bookingId);
        assertThat(tickets).hasSize(2);

        var payment = paymentRepository.findByLiqpayOrderId(ORDER_ID).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    private Exception attemptCallback(CountDownLatch readyLatch, CountDownLatch startLatch, String data,
                                      String signature) {
        try {
            readyLatch.countDown();
            startLatch.await();
            paymentStatusService.handleCallback(data, signature);
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
        return Movie.builder().title("Callback Concurrency Test Movie").slug("callback-concurrency-test-movie")
                .trailerUrl("https://example.com/trailer").description("Test movie for callback concurrency testing")
                .durationMinutes(120).releaseDate(LocalDate.now().minusDays(1))
                .endShowingDate(LocalDate.now().plusMonths(1)).status(MovieStatus.CURRENT)
                .posterFileName("poster.jpg").ageRating(AgeRating.PEGI_12).build();
    }
}
