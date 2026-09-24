package ua.lviv.bas.cinema.cinema.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.SeatReservation;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.booking.domain.status.ReservationStatus;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.booking.repository.SeatReservationRepository;
import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.movie.domain.enums.AgeRating;
import ua.lviv.bas.cinema.movie.domain.status.MovieStatus;
import ua.lviv.bas.cinema.movie.repository.MovieRepository;
import ua.lviv.bas.cinema.ticket.domain.Ticket;
import ua.lviv.bas.cinema.ticket.domain.TicketType;
import ua.lviv.bas.cinema.ticket.repository.TicketRepository;
import ua.lviv.bas.cinema.ticket.repository.TicketTypeRepository;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.repository.UserRepository;
import ua.lviv.bas.cinema.common.CinemaTime;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("ci")
@Import(TestcontainersConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SeatRepositoryTest {

    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private CinemaHallRepository cinemaHallRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private SeatReservationRepository seatReservationRepository;
    @Autowired
    private TicketTypeRepository ticketTypeRepository;
    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void findTicketedSeatIdsShouldReturnOnlySeatsWithTickets() {
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("ZZTEST Seat Repo Hall").build());
        var ticketedSeat = seatRepository.save(Seat.builder().row(1).number(1).x(0).y(0).hall(hall).build());
        var freeSeat = seatRepository.save(Seat.builder().row(1).number(2).x(60).y(0).hall(hall).build());

        var movie = movieRepository.save(buildMovie());
        var session = sessionRepository.save(Session.builder().movie(movie).hall(hall)
                .startTime(CinemaTime.now().plusDays(1)).basePrice(new BigDecimal("100.00")).build());
        var user = userRepository.save(buildUser());
        var booking = bookingRepository.save(Booking.builder().user(user).session(session)
                .status(BookingStatus.CONFIRMED).totalPrice(new BigDecimal("100.00"))
                .finalPrice(new BigDecimal("100.00")).expiresAt(Instant.now().plus(Duration.ofMinutes(20))).build());
        var seatReservation = seatReservationRepository.save(SeatReservation.builder().booking(booking)
                .seat(ticketedSeat).session(session).status(ReservationStatus.CONFIRMED)
                .reservedUntil(Instant.now().plus(Duration.ofMinutes(20))).build());
        var ticketType = ticketTypeRepository.save(TicketType.builder().displayName("ZZTEST Standard").build());
        ticketRepository.save(Ticket.builder().booking(booking).user(user).ticketType(ticketType)
                .seatReservation(seatReservation).originalPrice(new BigDecimal("100.00"))
                .finalPrice(new BigDecimal("100.00")).uniqueCode("ZZTEST-SEAT-REPO-1").build());

        var ticketedSeatIds = seatRepository
                .findTicketedSeatIds(List.of(ticketedSeat.getId(), freeSeat.getId()));

        assertThat(ticketedSeatIds).containsExactly(ticketedSeat.getId());
    }

    @Test
    void findTicketedSeatIdsShouldReturnEmptyWhenNoTickets() {
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("ZZTEST Seat Repo Hall 2").build());
        var seat = seatRepository.save(Seat.builder().row(1).number(1).x(0).y(0).hall(hall).build());

        var ticketedSeatIds = seatRepository.findTicketedSeatIds(List.of(seat.getId()));

        assertThat(ticketedSeatIds).isEmpty();
    }

    private Movie buildMovie() {
        return Movie.builder().title("ZZTEST Seat Repo Movie").slug("zztest-seat-repo-movie")
                .trailerUrl("https://example.com/trailer").description("Test movie for seat repository test")
                .durationMinutes(120).releaseDate(LocalDate.now().minusDays(1))
                .endShowingDate(LocalDate.now().plusMonths(1)).status(MovieStatus.CURRENT)
                .posterFileName("poster.jpg").ageRating(AgeRating.PEGI_12).build();
    }

    private User buildUser() {
        return User.builder().email("zztest.seat.repo@test.com").firstName("Test").lastName("User")
                .dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv").phoneNumber("+380000000016")
                .password("hashed-password").userRole(UserRole.ROLE_USER).enabled(true).emailVerified(true).build();
    }
}
