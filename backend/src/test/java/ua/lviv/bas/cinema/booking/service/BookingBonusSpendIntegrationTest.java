package ua.lviv.bas.cinema.booking.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ua.lviv.bas.cinema.bonus.domain.BonusCard;
import ua.lviv.bas.cinema.bonus.repository.BonusCardRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("ci")
@Import(TestcontainersConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BookingBonusSpendIntegrationTest {

    @Autowired
    private BookingService bookingService;
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
    @Autowired
    private BonusCardRepository bonusCardRepository;

    @Test
    @Timeout(10)
    void creatingBookingWithBonusPointsShouldNotHangOrFailOnCrossTransactionSpend() {
        var user = userRepository.save(User.builder().email("bonus.spend.integration@test.com").firstName("Test")
                .lastName("User").dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv").phoneNumber("+380000000099")
                .password("hashed-password").userRole(UserRole.ROLE_USER).enabled(true).build());
        bonusCardRepository.save(BonusCard.builder().user(user).pointsBalance(150).build());

        var movie = movieRepository.save(Movie.builder().title("Bonus Spend Test Movie")
                .slug("bonus-spend-test-movie").trailerUrl("https://example.com/trailer")
                .description("Test movie for bonus spend integration test").durationMinutes(120)
                .releaseDate(LocalDate.now().minusDays(1)).endShowingDate(LocalDate.now().plusMonths(1))
                .status(MovieStatus.CURRENT).posterFileName("poster.jpg").ageRating(AgeRating.PEGI_12).build());
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("Bonus Spend Hall").build());
        var seat = seatRepository.save(Seat.builder().hall(hall).row(1).number(1).build());
        var session = sessionRepository.save(Session.builder().movie(movie).hall(hall)
                .startTime(LocalDateTime.now().plusDays(1)).basePrice(new BigDecimal("200.00")).build());
        var ticketType = ticketTypeRepository.save(
                TicketType.builder().displayName("Adult").priceMultiplier(BigDecimal.ONE).build());

        var request = new BookingCreateRequest(session.getId(),
                List.of(new BookingCreateRequest.SeatSelectionRequest(seat.getId(), ticketType.getId())), 100);

        var response = bookingService.createBooking(request, user);

        assertThat(response.bonusPointsUsed()).isEqualTo(100);
        var refreshedCard = bonusCardRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(refreshedCard.getPointsBalance()).isEqualTo(50);
    }
}
