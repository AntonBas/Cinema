package ua.lviv.bas.cinema.refund.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.exception.domain.financial.refund.TicketNotRefundableException;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.movie.domain.enums.AgeRating;
import ua.lviv.bas.cinema.movie.domain.status.MovieStatus;
import ua.lviv.bas.cinema.movie.repository.MovieRepository;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.payment.repository.PaymentRepository;
import ua.lviv.bas.cinema.refund.domain.status.RefundStatus;
import ua.lviv.bas.cinema.refund.repository.RefundRepository;
import ua.lviv.bas.cinema.ticket.domain.Ticket;
import ua.lviv.bas.cinema.ticket.domain.TicketStatus;
import ua.lviv.bas.cinema.ticket.domain.TicketType;
import ua.lviv.bas.cinema.ticket.repository.TicketRepository;
import ua.lviv.bas.cinema.ticket.repository.TicketTypeRepository;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
class RefundCreationConcurrencyTest {

    private static final String TICKET_CODE = "TKT-REFUND-CONC";

    @Autowired
    private RefundTransactionExecutor refundTransactionExecutor;
    @Autowired
    private RefundRepository refundRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private PaymentRepository paymentRepository;
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
    private TicketTypeRepository ticketTypeRepository;

    private Long ticketId;
    private Long userId;

    @BeforeEach
    void setUp() {
        var user = userRepository.save(buildUser("refund.creation.concurrency@test.com"));
        userId = user.getId();
        var movie = movieRepository.save(buildMovie());
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("Refund Concur Hall").build());
        var session = sessionRepository.save(Session.builder().movie(movie).hall(hall)
                .startTime(LocalDateTime.now().plusDays(3)).basePrice(new BigDecimal("100.00")).build());
        var ticketType = ticketTypeRepository.save(TicketType.builder().displayName("Standard").build());

        var booking = bookingRepository.save(Booking.builder().user(user).session(session)
                .status(BookingStatus.CONFIRMED).totalPrice(new BigDecimal("100.00"))
                .finalPrice(new BigDecimal("100.00")).expiresAt(LocalDateTime.now().plusMinutes(20)).build());

        var payment = paymentRepository.save(Payment.builder().booking(booking).amount(new BigDecimal("100.00"))
                .status(PaymentStatus.SUCCESS).liqpayOrderId("ORD_REFUND_CONCURRENCY")
                .liqpayPaymentId("PAY_REFUND_CONCURRENCY").build());

        var ticket = ticketRepository.save(Ticket.builder().booking(booking).user(user).ticketType(ticketType)
                .payment(payment).purchaseTime(LocalDateTime.now().minusHours(1))
                .originalPrice(new BigDecimal("100.00")).finalPrice(new BigDecimal("100.00")).uniqueCode(TICKET_CODE)
                .status(TicketStatus.ACTIVE).build());
        ticketId = ticket.getId();
    }

    @Test
    void concurrentRefundRequestsOnlyOneShouldCreateProcessingRefund() throws Exception {
        var readyLatch = new CountDownLatch(2);
        var startLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Exception> attempt = () -> attemptCreateRefund(readyLatch, startLatch);

        Future<Exception> resultA = executor.submit(attempt);
        Future<Exception> resultB = executor.submit(attempt);

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();

        Exception outcomeA = resultA.get(10, TimeUnit.SECONDS);
        Exception outcomeB = resultB.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        long successCount = Stream.of(outcomeA, outcomeB).filter(Objects::isNull).count();
        long alreadyProcessingCount = Stream.of(outcomeA, outcomeB)
                .filter(e -> e instanceof TicketNotRefundableException).count();

        assertThat(successCount).isEqualTo(1);
        assertThat(alreadyProcessingCount).isEqualTo(1);

        var processingRefunds = refundRepository.findByUserIdOrderByCreatedDateDesc(userId).stream()
                .filter(refund -> refund.getStatus() == RefundStatus.PROCESSING).toList();
        assertThat(processingRefunds).hasSize(1);
    }

    private Exception attemptCreateRefund(CountDownLatch readyLatch, CountDownLatch startLatch) {
        try {
            readyLatch.countDown();
            startLatch.await();
            refundTransactionExecutor.createProcessingRefund(ticketId, userId, "Concurrency test reason");
            return null;
        } catch (Exception e) {
            return e;
        }
    }

    private User buildUser(String email) {
        return User.builder().email(email).firstName("Test").lastName("User")
                .dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv").phoneNumber("+380000000014")
                .password("hashed-password").userRole(UserRole.ROLE_USER).enabled(true).build();
    }

    private Movie buildMovie() {
        return Movie.builder().title("Refund Concurrency Test Movie").slug("refund-concurrency-test-movie")
                .trailerUrl("https://example.com/trailer").description("Test movie for refund creation concurrency")
                .durationMinutes(120).releaseDate(LocalDate.now().minusDays(1))
                .endShowingDate(LocalDate.now().plusMonths(1)).status(MovieStatus.CURRENT)
                .posterFileName("poster.jpg").ageRating(AgeRating.PEGI_12).build();
    }
}
