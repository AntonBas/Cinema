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
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.domain.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.cinema.dto.session.response.SessionAdminResponse;
import ua.lviv.bas.cinema.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.config.NoOpCacheTestConfig;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.movie.domain.enums.AgeRating;
import ua.lviv.bas.cinema.movie.domain.status.MovieStatus;
import ua.lviv.bas.cinema.movie.repository.MovieRepository;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.payment.repository.PaymentRepository;
import ua.lviv.bas.cinema.refund.domain.Refund;
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
import ua.lviv.bas.cinema.common.CinemaTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("ci")
@Import({TestcontainersConfig.class, NoOpCacheTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SessionServiceIntegrationTest {

    @Autowired
    private SessionService sessionService;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CinemaHallRepository cinemaHallRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private TicketTypeRepository ticketTypeRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private RefundRepository refundRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void getScheduleAndGetSessionsShouldReturnCorrectAggregatedValues() {
        var movie = movieRepository.save(buildMovie());
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("ZZTEST Hall").build());
        for (int i = 1; i <= 5; i++) {
            seatRepository.save(Seat.builder().row(1).number(i).x(0).y(0).hall(hall).build());
        }

        var startTime = CinemaTime.now().plusDays(1);
        var session = sessionRepository.save(Session.builder().movie(movie).hall(hall)
                .startTime(startTime).basePrice(new BigDecimal("100.00")).build());

        var user = userRepository.save(buildUser());
        var ticketType = ticketTypeRepository.save(TicketType.builder().displayName("ZZTEST Aggregation").build());

        var discountedBooking = buildBooking(user, session, BookingStatus.CONFIRMED, new BigDecimal("100.00"));
        discountedBooking.setFinalPrice(new BigDecimal("90.00"));
        bookingRepository.save(discountedBooking);
        saveTicket(discountedBooking, ticketType, "ZZTEST-A1", TicketStatus.ACTIVE);
        saveTicket(discountedBooking, ticketType, "ZZTEST-A2", TicketStatus.ACTIVE);

        var partlyRefundedBooking = bookingRepository
                .save(buildBooking(user, session, BookingStatus.CONFIRMED, new BigDecimal("150.00")));
        saveTicket(partlyRefundedBooking, ticketType, "ZZTEST-B1", TicketStatus.ACTIVE);
        var refundedTicket = saveTicket(partlyRefundedBooking, ticketType, "ZZTEST-B2", TicketStatus.REFUNDED);
        var payment = paymentRepository.save(Payment.builder().booking(partlyRefundedBooking)
                .amount(new BigDecimal("150.00")).status(PaymentStatus.SUCCESS).build());
        refundRepository.save(Refund.builder().payment(payment).user(user).ticket(refundedTicket)
                .totalAmount(new BigDecimal("60.00")).status(RefundStatus.PROCESSED).build());

        bookingRepository.save(buildBooking(user, session, BookingStatus.PENDING, new BigDecimal("999.00")));

        var schedule = sessionService.getSchedule(null, startTime.toLocalDate(), movie.getId());
        var scheduleEntry = schedule.stream().filter(s -> s.id().equals(session.getId())).findFirst().orElseThrow();
        assertThat(scheduleEntry.hallCapacity()).isEqualTo(5);
        assertThat(scheduleEntry.movieSlug()).isEqualTo(movie.getSlug());

        var adminPage = sessionService.getSessions(hall.getId(), null, null, null, null, PageRequest.of(0, 10));
        var adminEntry = adminPage.getContent().stream().filter(s -> s.id().equals(session.getId())).findFirst()
                .orElseThrow();
        assertThat(adminEntry.hallCapacity()).isEqualTo(5);
        assertThat(adminEntry.ticketsSold()).isEqualTo(3);
        assertThat(adminEntry.totalRevenue()).isEqualByComparingTo(new BigDecimal("180.00"));
    }

    @Test
    void getSessionsShouldListActiveSessionsSoonestFirstThenPastSessionsNewestFirst() {
        var movie = movieRepository.save(buildMovie("ZZTEST Order Movie", "zztest-order-movie"));
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("ZZTEST Order Hall").build());
        var now = CinemaTime.now().withNano(0);

        var laterScheduled = saveSession(movie, hall, now.plusDays(3), "100.00", CinemaSessionStatus.SCHEDULED);
        var soonerScheduled = saveSession(movie, hall, now.plusDays(1), "100.00", CinemaSessionStatus.SCHEDULED);
        var cancelled = saveSession(movie, hall, now.plusDays(2), "100.00", CinemaSessionStatus.CANCELLED);
        var olderCompleted = saveSession(movie, hall, now.minusDays(2), "100.00", CinemaSessionStatus.COMPLETED);
        var newerCompleted = saveSession(movie, hall, now.minusDays(1), "100.00", CinemaSessionStatus.COMPLETED);

        var page = sessionService.getSessions(hall.getId(), null, null, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(SessionAdminResponse::id).containsExactly(soonerScheduled.getId(),
                laterScheduled.getId(), cancelled.getId(), newerCompleted.getId(), olderCompleted.getId());
    }

    @Test
    void getSessionsShouldApplyRequestedSortWithStartTimeAsTiebreaker() {
        var movie = movieRepository.save(buildMovie("ZZTEST Sort Movie", "zztest-sort-movie"));
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("ZZTEST Sort Hall").build());
        var now = CinemaTime.now().withNano(0);

        var cheapLater = saveSession(movie, hall, now.plusDays(2), "100.00", CinemaSessionStatus.SCHEDULED);
        var expensive = saveSession(movie, hall, now.plusDays(3), "300.00", CinemaSessionStatus.SCHEDULED);
        var cheapSooner = saveSession(movie, hall, now.plusDays(1), "100.00", CinemaSessionStatus.SCHEDULED);

        var page = sessionService.getSessions(hall.getId(), null, null, null, null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "basePrice")));

        assertThat(page.getContent()).extracting(SessionAdminResponse::id).containsExactly(expensive.getId(),
                cheapSooner.getId(), cheapLater.getId());
    }

    @Test
    void getScheduleDatesShouldReturnDistinctUpcomingScheduledDatesInOrder() {
        var movie = movieRepository.save(buildMovie("ZZTEST Dates Movie", "zztest-dates-movie"));
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("ZZTEST Dates Hall").build());
        var inTwoDays = CinemaTime.today().plusDays(2);
        var inFiveDays = CinemaTime.today().plusDays(5);

        saveSession(movie, hall, inFiveDays.atTime(18, 0), "100.00", CinemaSessionStatus.SCHEDULED);
        saveSession(movie, hall, inTwoDays.atTime(12, 0), "100.00", CinemaSessionStatus.SCHEDULED);
        saveSession(movie, hall, inTwoDays.atTime(20, 0), "100.00", CinemaSessionStatus.SCHEDULED);
        saveSession(movie, hall, CinemaTime.today().plusDays(3).atTime(15, 0), "100.00", CinemaSessionStatus.CANCELLED);
        saveSession(movie, hall, CinemaTime.today().minusDays(1).atTime(15, 0), "100.00", CinemaSessionStatus.COMPLETED);

        assertThat(sessionService.getScheduleDates(movie.getId())).containsExactly(inTwoDays, inFiveDays);
    }

    @Test
    void getSessionsShouldRejectUnknownSortProperty() {
        var pageable = PageRequest.of(0, 10, Sort.by("unknownProperty"));

        assertThatThrownBy(() -> sessionService.getSessions(null, null, null, null, null, pageable))
                .isInstanceOf(PropertyReferenceException.class);
    }

    private Session saveSession(Movie movie, CinemaHall hall, LocalDateTime startTime, String basePrice,
                                CinemaSessionStatus status) {
        return sessionRepository.save(Session.builder().movie(movie).hall(hall).startTime(startTime)
                .basePrice(new BigDecimal(basePrice)).status(status).build());
    }

    private Movie buildMovie() {
        return buildMovie("ZZTEST Session Movie", "zztest-session-movie");
    }

    private Movie buildMovie(String title, String slug) {
        return Movie.builder().title(title).slug(slug)
                .trailerUrl("https://example.com/trailer").description("Test movie for session regression test")
                .durationMinutes(120).releaseDate(CinemaTime.today().minusDays(1))
                .endShowingDate(CinemaTime.today().plusMonths(1)).status(MovieStatus.CURRENT)
                .posterFileName("poster.jpg").ageRating(AgeRating.PEGI_12).build();
    }

    private User buildUser() {
        return User.builder().email("zztest.session@test.com").firstName("Test").lastName("User")
                .dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv").phoneNumber("+380000000015")
                .password("hashed-password").userRole(UserRole.ROLE_USER).enabled(true).emailVerified(true).build();
    }

    private Ticket saveTicket(Booking booking, TicketType ticketType, String code, TicketStatus status) {
        return ticketRepository.save(Ticket.builder().booking(booking).user(booking.getUser()).ticketType(ticketType)
                .originalPrice(new BigDecimal("75.00")).finalPrice(new BigDecimal("75.00")).uniqueCode(code)
                .status(status).build());
    }

    private Booking buildBooking(User user, Session session, BookingStatus status, BigDecimal totalPrice) {
        return Booking.builder().user(user).session(session).status(status).totalPrice(totalPrice)
                .finalPrice(totalPrice).expiresAt(Instant.now().plus(Duration.ofMinutes(20))).build();
    }
}
