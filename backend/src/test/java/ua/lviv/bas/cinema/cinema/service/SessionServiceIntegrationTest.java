package ua.lviv.bas.cinema.cinema.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.config.NoOpCacheTestConfig;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.movie.domain.enums.AgeRating;
import ua.lviv.bas.cinema.movie.domain.status.MovieStatus;
import ua.lviv.bas.cinema.movie.repository.MovieRepository;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

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
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void getScheduleAndGetSessionsShouldReturnCorrectAggregatedValues() {
        var movie = movieRepository.save(buildMovie());
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("ZZTEST Hall").build());
        for (int i = 1; i <= 5; i++) {
            seatRepository.save(Seat.builder().row(1).number(i).hall(hall).build());
        }

        var session = sessionRepository.save(Session.builder().movie(movie).hall(hall)
                .startTime(LocalDateTime.now().plusDays(1)).basePrice(new BigDecimal("100.00")).build());

        var user = userRepository.save(buildUser());
        bookingRepository.save(buildBooking(user, session, BookingStatus.CONFIRMED, new BigDecimal("100.00")));
        bookingRepository.save(buildBooking(user, session, BookingStatus.CONFIRMED, new BigDecimal("150.00")));
        bookingRepository.save(buildBooking(user, session, BookingStatus.PENDING, new BigDecimal("999.00")));

        var schedule = sessionService.getSchedule(null, null, movie.getId());
        var scheduleEntry = schedule.stream().filter(s -> s.id().equals(session.getId())).findFirst().orElseThrow();
        assertThat(scheduleEntry.hallCapacity()).isEqualTo(5);

        var adminPage = sessionService.getSessions(hall.getId(), null, null, null, null, PageRequest.of(0, 10));
        var adminEntry = adminPage.getContent().stream().filter(s -> s.id().equals(session.getId())).findFirst()
                .orElseThrow();
        assertThat(adminEntry.hallCapacity()).isEqualTo(5);
        assertThat(adminEntry.ticketsSold()).isEqualTo(2);
        assertThat(adminEntry.totalRevenue()).isEqualByComparingTo(new BigDecimal("250.00"));
    }

    private Movie buildMovie() {
        return Movie.builder().title("ZZTEST Session Movie").slug("zztest-session-movie")
                .trailerUrl("https://example.com/trailer").description("Test movie for session regression test")
                .durationMinutes(120).releaseDate(LocalDate.now().minusDays(1))
                .endShowingDate(LocalDate.now().plusMonths(1)).status(MovieStatus.CURRENT)
                .posterFileName("poster.jpg").ageRating(AgeRating.PEGI_12).build();
    }

    private User buildUser() {
        return User.builder().email("zztest.session@test.com").firstName("Test").lastName("User")
                .dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv").phoneNumber("+380000000015")
                .password("hashed-password").userRole(UserRole.ROLE_USER).enabled(true).build();
    }

    private Booking buildBooking(User user, Session session, BookingStatus status, BigDecimal totalPrice) {
        return Booking.builder().user(user).session(session).status(status).totalPrice(totalPrice)
                .finalPrice(totalPrice).expiresAt(LocalDateTime.now().plusMinutes(20)).build();
    }
}
