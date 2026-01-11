package ua.lviv.bas.cinema.service.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.BookedSeat;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.enums.BookedSeatStatus;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatAvailabilityResponse;
import ua.lviv.bas.cinema.exception.domain.booking.SeatNotAvailableException;
import ua.lviv.bas.cinema.exception.domain.cinema.SeatNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.repository.BookedSeatRepository;
import ua.lviv.bas.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.repository.TicketTypeRepository;

@ExtendWith(MockitoExtension.class)
public class SeatAvailabilityServiceTest {

	@Mock
	private SessionRepository sessionRepository;

	@Mock
	private SeatRepository seatRepository;

	@Mock
	private BookedSeatRepository bookedSeatRepository;

	@Mock
	private TicketTypeRepository ticketTypeRepository;

	@InjectMocks
	private SeatAvailabilityService seatAvailabilityService;

	private Session testSession;
	private CinemaHall testHall;
	private Seat testSeat1;
	private Seat testSeat2;
	private Seat inactiveSeat;
	private TicketType adultTicketType;
	private TicketType childTicketType;
	private final Long SESSION_ID = 1L;
	private final Long HALL_ID = 2L;
	private final Long SEAT_ID_1 = 3L;
	private final Long SEAT_ID_2 = 4L;
	private final Long INACTIVE_SEAT_ID = 5L;
	private final BigDecimal BASE_PRICE = new BigDecimal("200.00");

	private static final List<BookedSeatStatus> BOOKED_STATUSES = Arrays.asList(BookedSeatStatus.PENDING,
			BookedSeatStatus.CONFIRMED);

	@BeforeEach
	void setUp() {
		Movie testMovie = new Movie();
		testMovie.setTitle("Test Movie");

		testHall = new CinemaHall();
		testHall.setId(HALL_ID);
		testHall.setName("Hall A");

		testSession = new Session();
		testSession.setId(SESSION_ID);
		testSession.setMovie(testMovie);
		testSession.setHall(testHall);
		testSession.setBasePrice(BASE_PRICE);

		testSeat1 = new Seat();
		testSeat1.setId(SEAT_ID_1);
		testSeat1.setRow(1);
		testSeat1.setNumber(1);
		testSeat1.setSeatType(SeatType.STANDARD);
		testSeat1.setActive(true);
		testSeat1.setHall(testHall);

		testSeat2 = new Seat();
		testSeat2.setId(SEAT_ID_2);
		testSeat2.setRow(1);
		testSeat2.setNumber(2);
		testSeat2.setSeatType(SeatType.VIP);
		testSeat2.setActive(true);
		testSeat2.setHall(testHall);

		inactiveSeat = new Seat();
		inactiveSeat.setId(INACTIVE_SEAT_ID);
		inactiveSeat.setRow(2);
		inactiveSeat.setNumber(1);
		inactiveSeat.setSeatType(SeatType.STANDARD);
		inactiveSeat.setActive(false);
		inactiveSeat.setHall(testHall);

		adultTicketType = new TicketType();
		adultTicketType.setId(1L);
		adultTicketType.setDisplayName("Adult");
		adultTicketType.setPriceMultiplier(BigDecimal.ONE);
		adultTicketType.setActive(true);

		childTicketType = new TicketType();
		childTicketType.setId(2L);
		childTicketType.setDisplayName("Child");
		childTicketType.setPriceMultiplier(new BigDecimal("0.7"));
		childTicketType.setActive(true);
	}

	@Test
	void getSeatAvailability_Success() {
		List<Seat> allSeats = Arrays.asList(testSeat1, testSeat2, inactiveSeat);
		List<BookedSeat> bookedSeats = Collections.emptyList();
		List<TicketType> activeTicketTypes = Arrays.asList(adultTicketType, childTicketType);

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByHallId(HALL_ID)).thenReturn(allSeats);
		when(bookedSeatRepository.findBySessionIdAndStatusIn(SESSION_ID, BOOKED_STATUSES)).thenReturn(bookedSeats);
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(activeTicketTypes);

		SeatAvailabilityResponse result = seatAvailabilityService.getSeatAvailability(SESSION_ID);

		assertThat(result).isNotNull();
		assertThat(result.getSessionId()).isEqualTo(SESSION_ID);
		assertThat(result.getMovieTitle()).isEqualTo("Test Movie");
		assertThat(result.getBasePrice()).isEqualTo(BASE_PRICE);
		assertThat(result.getHallName()).isEqualTo("Hall A");
		assertThat(result.getAvailableSeats()).isEqualTo(2);
		assertThat(result.getSeats()).hasSize(3);
	}

	@Test
	void getSeatAvailability_WhenSessionNotFound_ShouldThrowException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> seatAvailabilityService.getSeatAvailability(SESSION_ID))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void getSeatAvailability_WithBookedSeats() {
		List<Seat> allSeats = Arrays.asList(testSeat1, testSeat2);
		BookedSeat bookedSeat = new BookedSeat();
		bookedSeat.setSeat(testSeat1);
		bookedSeat.setStatus(BookedSeatStatus.CONFIRMED);
		List<BookedSeat> bookedSeats = Arrays.asList(bookedSeat);
		List<TicketType> activeTicketTypes = Arrays.asList(adultTicketType);

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByHallId(HALL_ID)).thenReturn(allSeats);
		when(bookedSeatRepository.findBySessionIdAndStatusIn(SESSION_ID, BOOKED_STATUSES)).thenReturn(bookedSeats);
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(activeTicketTypes);

		SeatAvailabilityResponse result = seatAvailabilityService.getSeatAvailability(SESSION_ID);

		assertThat(result.getAvailableSeats()).isEqualTo(1);
	}

	@Test
	void validateSeatAvailability_Success() {
		when(bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, SEAT_ID_1, BOOKED_STATUSES))
				.thenReturn(false);
		when(seatRepository.findById(SEAT_ID_1)).thenReturn(Optional.of(testSeat1));

		seatAvailabilityService.validateSeatAvailability(SESSION_ID, SEAT_ID_1);
	}

	@Test
	void validateSeatAvailability_WhenSeatBooked_ShouldThrowException() {
		when(bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, SEAT_ID_1, BOOKED_STATUSES))
				.thenReturn(true);

		assertThatThrownBy(() -> seatAvailabilityService.validateSeatAvailability(SESSION_ID, SEAT_ID_1))
				.isInstanceOf(SeatNotAvailableException.class);
	}

	@Test
	void validateSeatAvailability_WhenSeatInactive_ShouldThrowException() {
		when(bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, INACTIVE_SEAT_ID, BOOKED_STATUSES))
				.thenReturn(false);
		when(seatRepository.findById(INACTIVE_SEAT_ID)).thenReturn(Optional.of(inactiveSeat));

		assertThatThrownBy(() -> seatAvailabilityService.validateSeatAvailability(SESSION_ID, INACTIVE_SEAT_ID))
				.isInstanceOf(SeatNotAvailableException.class);
	}

	@Test
	void validateSeatAvailability_WhenSeatNotFound_ShouldThrowException() {
		Long nonExistentSeatId = 999L;
		when(bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, nonExistentSeatId, BOOKED_STATUSES))
				.thenReturn(false);
		when(seatRepository.findById(nonExistentSeatId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> seatAvailabilityService.validateSeatAvailability(SESSION_ID, nonExistentSeatId))
				.isInstanceOf(SeatNotFoundException.class);
	}

	@Test
	void calculateSeatPrice_StandardSeatAdultTicket() {
		BigDecimal result = seatAvailabilityService.calculateSeatPrice(testSession, testSeat1, adultTicketType);

		BigDecimal expected = BASE_PRICE.multiply(testSeat1.getSeatType().getPriceMultiplier())
				.multiply(adultTicketType.getPriceMultiplier());

		assertThat(result).isEqualByComparingTo(expected);
	}

	@Test
	void calculateSeatPrice_VIPSeatChildTicket() {
		BigDecimal result = seatAvailabilityService.calculateSeatPrice(testSession, testSeat2, childTicketType);

		BigDecimal expected = BASE_PRICE.multiply(testSeat2.getSeatType().getPriceMultiplier())
				.multiply(childTicketType.getPriceMultiplier());

		assertThat(result).isEqualByComparingTo(expected);
	}

	@Test
	void calculateSeatPrice_WithNullTicketType() {
		BigDecimal result = seatAvailabilityService.calculateSeatPrice(testSession, testSeat1, null);

		BigDecimal expected = BASE_PRICE.multiply(testSeat1.getSeatType().getPriceMultiplier());

		assertThat(result).isEqualByComparingTo(expected);
	}

	@Test
	void isSeatAvailableForSession_WhenAvailable() {
		when(bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, SEAT_ID_1, BOOKED_STATUSES))
				.thenReturn(false);

		boolean result = seatAvailabilityService.isSeatAvailableForSession(SESSION_ID, SEAT_ID_1);

		assertThat(result).isTrue();
	}

	@Test
	void isSeatAvailableForSession_WhenNotAvailable() {
		when(bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, SEAT_ID_1, BOOKED_STATUSES))
				.thenReturn(true);

		boolean result = seatAvailabilityService.isSeatAvailableForSession(SESSION_ID, SEAT_ID_1);

		assertThat(result).isFalse();
	}

	@Test
	void getAvailableSeatsCount_Success() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.countByHallId(HALL_ID)).thenReturn(100L);
		when(bookedSeatRepository.countBySessionIdAndStatusIn(SESSION_ID, BOOKED_STATUSES)).thenReturn(25L);

		int result = seatAvailabilityService.getAvailableSeatsCount(SESSION_ID);

		assertThat(result).isEqualTo(75);
	}

	@Test
	void getAvailableSeatsCount_WhenSessionNotFound_ShouldThrowException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> seatAvailabilityService.getAvailableSeatsCount(SESSION_ID))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void getSeatAvailability_WithTemporarilyReservedSeat() {
		List<Seat> allSeats = Arrays.asList(testSeat1, testSeat2);
		BookedSeat pendingSeat = new BookedSeat();
		pendingSeat.setSeat(testSeat1);
		pendingSeat.setStatus(BookedSeatStatus.PENDING);
		List<BookedSeat> bookedSeats = Arrays.asList(pendingSeat);
		List<TicketType> activeTicketTypes = Arrays.asList(adultTicketType);

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByHallId(HALL_ID)).thenReturn(allSeats);
		when(bookedSeatRepository.findBySessionIdAndStatusIn(SESSION_ID, BOOKED_STATUSES)).thenReturn(bookedSeats);
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(activeTicketTypes);

		SeatAvailabilityResponse result = seatAvailabilityService.getSeatAvailability(SESSION_ID);

		SeatAvailabilityResponse.SeatInfo pendingSeatInfo = result.getSeats().stream()
				.filter(s -> s.getId().equals(SEAT_ID_1)).findFirst().orElseThrow();
		assertThat(pendingSeatInfo.getAvailable()).isFalse();
		assertThat(pendingSeatInfo.getTemporarilyReserved()).isTrue();
	}

	@Test
	void getSeatAvailability_WithNoActiveTicketTypes() {
		List<Seat> allSeats = Arrays.asList(testSeat1);
		List<BookedSeat> bookedSeats = Collections.emptyList();

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByHallId(HALL_ID)).thenReturn(allSeats);
		when(bookedSeatRepository.findBySessionIdAndStatusIn(SESSION_ID, BOOKED_STATUSES)).thenReturn(bookedSeats);
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(Collections.emptyList());

		SeatAvailabilityResponse result = seatAvailabilityService.getSeatAvailability(SESSION_ID);

		SeatAvailabilityResponse.SeatInfo seatInfo = result.getSeats().get(0);
		assertThat(seatInfo.getTicketPrices()).isEmpty();
	}

	@Test
	void getAvailableSeatsCount_WhenAllSeatsAvailable() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.countByHallId(HALL_ID)).thenReturn(50L);
		when(bookedSeatRepository.countBySessionIdAndStatusIn(SESSION_ID, BOOKED_STATUSES)).thenReturn(0L);

		int result = seatAvailabilityService.getAvailableSeatsCount(SESSION_ID);

		assertThat(result).isEqualTo(50);
	}

	@Test
	void getAvailableSeatsCount_WhenAllSeatsBooked() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.countByHallId(HALL_ID)).thenReturn(30L);
		when(bookedSeatRepository.countBySessionIdAndStatusIn(SESSION_ID, BOOKED_STATUSES)).thenReturn(30L);

		int result = seatAvailabilityService.getAvailableSeatsCount(SESSION_ID);

		assertThat(result).isEqualTo(0);
	}
}