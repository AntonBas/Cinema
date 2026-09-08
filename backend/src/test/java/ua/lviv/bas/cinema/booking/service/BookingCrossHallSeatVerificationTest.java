package ua.lviv.bas.cinema.booking.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ua.lviv.bas.cinema.booking.dto.request.BookingCreateRequest;
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
import ua.lviv.bas.cinema.ticket.domain.TicketType;
import ua.lviv.bas.cinema.ticket.repository.TicketTypeRepository;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import ua.lviv.bas.cinema.exception.domain.booking.SeatNotAvailableException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("ci")
@Import(TestcontainersConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BookingCrossHallSeatVerificationTest {

    @Autowired
    private BookingCreationService bookingCreationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CinemaHallRepository cinemaHallRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Test
    void createAndPersistShouldRejectASeatFromADifferentHallThanTheSession() {
        var user = userRepository.save(User.builder().email("cross.hall.seat@test.com").firstName("Test")
                .lastName("User").dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv").phoneNumber("+380000000077")
                .password("hashed-password").userRole(UserRole.ROLE_USER).enabled(true).build());

        var movie = movieRepository.save(Movie.builder().title("Cross Hall Test Movie")
                .slug("cross-hall-test-movie").trailerUrl("https://example.com/trailer")
                .description("Test movie for cross-hall seat verification").durationMinutes(120)
                .releaseDate(LocalDate.now().minusDays(1)).endShowingDate(LocalDate.now().plusMonths(1))
                .status(MovieStatus.CURRENT).posterFileName("poster.jpg").ageRating(AgeRating.PEGI_12).build());

        var sessionHall = cinemaHallRepository.save(CinemaHall.builder().name("Session Hall").build());
        var otherHall = cinemaHallRepository.save(CinemaHall.builder().name("Different Hall").build());

        var seatInOtherHall = seatRepository.save(Seat.builder().hall(otherHall).row(1).number(1).build());

        var session = sessionRepository.save(Session.builder().movie(movie).hall(sessionHall)
                .startTime(LocalDateTime.now().plusDays(1)).basePrice(new BigDecimal("200.00")).build());
        var ticketType = ticketTypeRepository
                .save(TicketType.builder().displayName("Adult").priceMultiplier(BigDecimal.ONE).build());

        var request = new BookingCreateRequest(session.getId(),
                List.of(new BookingCreateRequest.SeatSelectionRequest(seatInOtherHall.getId(), ticketType.getId())),
                0);

        assertThat(session.getHall().getId()).isNotEqualTo(otherHall.getId());

        assertThatThrownBy(() -> bookingCreationService.createAndPersist(request, user))
                .isInstanceOf(SeatNotAvailableException.class);
    }
}
