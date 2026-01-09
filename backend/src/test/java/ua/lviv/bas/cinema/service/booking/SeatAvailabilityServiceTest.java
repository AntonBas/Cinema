package ua.lviv.bas.cinema.service.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
import ua.lviv.bas.cinema.repository.BookedSeatRepository;
import ua.lviv.bas.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.repository.TicketTypeRepository;

@ExtendWith(MockitoExtension.class)
class SeatAvailabilityServiceTest {

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
	private Movie testMovie;
	private Seat testSeat1;
	private Seat testSeat2;
	private Seat testSeat3;
	private TicketType adultTicketType;
	private TicketType childTicketType;
	private BookedSeat bookedSeat;
	private BookedSeat pendingSeat;
	private final Long SESSION_ID = 1L;
	private final Long HALL_ID = 2L;
	private final Long SEAT_ID_1 = 3L;
	private final Long SEAT_ID_2 = 4L;
	private final Long SEAT_ID_3 = 5L;
	private final Long MOVIE_ID = 6L;
	private final BigDecimal BASE_PRICE = new BigDecimal("200.00");

	@BeforeEach
	void setUp() {
		testMovie = new Movie();
		testMovie.setId(MOVIE_ID);
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

		testSeat3 = new Seat();
		testSeat3.setId(SEAT_ID_3);
		testSeat3.setRow(2);
		testSeat3.setNumber(1);
		testSeat3.setSeatType(SeatType.STANDARD);
		testSeat3.setActive(false);
		testSeat3.setHall(testHall);

		adultTicketType = new TicketType();
		adultTicketType.setId(1L);
		adultTicketType.setDisplayName("Adult");
		adultTicketType.setPriceMultiplier(new BigDecimal("1.0"));
		adultTicketType.setActive(true);

		childTicketType = new TicketType();
		childTicketType.setId(2L);
		childTicketType.setDisplayName("Child");
		childTicketType.setPriceMultiplier(new BigDecimal("0.7"));
		childTicketType.setActive(true);

		bookedSeat = new BookedSeat();
		bookedSeat.setId(1L);
		bookedSeat.setSeat(testSeat2);
		bookedSeat.setSession(testSession);
		bookedSeat.setStatus(BookedSeatStatus.CONFIRMED);

		pendingSeat = new BookedSeat();
		pendingSeat.setId(2L);
		pendingSeat.setSeat(testSeat1);
		pendingSeat.setSession(testSession);
		pendingSeat.setStatus(BookedSeatStatus.PENDING);
	}

	@Test
	void getSeatAvailability_Success() {
		List<Seat> allSeats = Arrays.asList(testSeat1, testSeat2, testSeat3);
		List<BookedSeat> bookedSeats = Arrays.asList(bookedSeat, pendingSeat);
		List<TicketType> activeTicketTypes = Arrays.asList(adultTicketType, childTicketType);

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByHallId(HALL_ID)).thenReturn(allSeats);
		when(bookedSeatRepository.findBySessionIdAndStatusIn(SESSION_ID,
				Arrays.asList(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED))).thenReturn(bookedSeats);
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(activeTicketTypes);

		SeatAvailabilityResponse result = seatAvailabilityService.getSeatAvailability(SESSION_ID);

		assertThat(result).isNotNull();
		assertThat(result.getSessionId()).isEqualTo(SESSION_ID);
		assertThat(result.getMovieTitle()).isEqualTo("Test Movie");
		assertThat(result.getBasePrice()).isEqualTo(BASE_PRICE);
		assertThat(result.getHallName()).isEqualTo("Hall A");
		assertThat(result.getAvailableSeats()).isEqualTo(0);
		assertThat(result.getSeats()).hasSize(3);

		SeatAvailabilityResponse.SeatInfo seat1Info = result.getSeats().stream()
				.filter(s -> s.getId().equals(SEAT_ID_1)).findFirst().orElseThrow();
		assertThat(seat1Info.getAvailable()).isFalse();
		assertThat(seat1Info.getTemporarilyReserved()).isTrue();
		assertThat(seat1Info.getTicketPrices()).hasSize(2);

		SeatAvailabilityResponse.SeatInfo seat2Info = result.getSeats().stream()
				.filter(s -> s.getId().equals(SEAT_ID_2)).findFirst().orElseThrow();
		assertThat(seat2Info.getAvailable()).isFalse();
		assertThat(seat2Info.getTemporarilyReserved()).isFalse();

		SeatAvailabilityResponse.SeatInfo seat3Info = result.getSeats().stream()
				.filter(s -> s.getId().equals(SEAT_ID_3)).findFirst().orElseThrow();
		assertThat(seat3Info.getAvailable()).isFalse();

		verifyPriceCalculations(seat1Info);
	}

	@Test
	void getSeatAvailability_WhenSessionNotFound_ShouldThrowException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> seatAvailabilityService.getSeatAvailability(SESSION_ID))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Session not found");

		verify(sessionRepository).findById(SESSION_ID);
	}

	@Test
	void getSeatAvailability_WhenNoBookedSeats_ShouldReturnAllAvailable() {
		List<Seat> allSeats = Arrays.asList(testSeat1, testSeat2);
		List<TicketType> activeTicketTypes = Arrays.asList(adultTicketType);

		testSeat3.setActive(true);

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByHallId(HALL_ID)).thenReturn(allSeats);
		when(bookedSeatRepository.findBySessionIdAndStatusIn(SESSION_ID,
				Arrays.asList(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED)))
				.thenReturn(Collections.emptyList());
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(activeTicketTypes);

		SeatAvailabilityResponse result = seatAvailabilityService.getSeatAvailability(SESSION_ID);

		assertThat(result.getAvailableSeats()).isEqualTo(2);

		result.getSeats().forEach(seat -> {
			assertThat(seat.getAvailable()).isTrue();
			assertThat(seat.getTemporarilyReserved()).isFalse();
		});
	}

	@Test
	void validateSeatAvailability_Success() {
		when(bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, SEAT_ID_1,
				Arrays.asList(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED))).thenReturn(false);
		when(seatRepository.findById(SEAT_ID_1)).thenReturn(Optional.of(testSeat1));

		seatAvailabilityService.validateSeatAvailability(SESSION_ID, SEAT_ID_1);
	}

	@Test
	void validateSeatAvailability_WhenSeatBooked_ShouldThrowException() {
		when(bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, SEAT_ID_1,
				Arrays.asList(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED))).thenReturn(true);

		assertThatThrownBy(() -> seatAvailabilityService.validateSeatAvailability(SESSION_ID, SEAT_ID_1))
				.isInstanceOf(SeatNotAvailableException.class).hasMessageContaining(SEAT_ID_1.toString())
				.hasMessageContaining(SESSION_ID.toString());

		verify(bookedSeatRepository).existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, SEAT_ID_1,
				Arrays.asList(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED));
	}

	@Test
	void validateSeatAvailability_WhenSeatInactive_ShouldThrowException() {
		when(bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, SEAT_ID_3,
				Arrays.asList(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED))).thenReturn(false);
		when(seatRepository.findById(SEAT_ID_3)).thenReturn(Optional.of(testSeat3));

		assertThatThrownBy(() -> seatAvailabilityService.validateSeatAvailability(SESSION_ID, SEAT_ID_3))
				.isInstanceOf(SeatNotAvailableException.class).hasMessage("Seat 5 is not active");

		verify(seatRepository).findById(SEAT_ID_3);
	}

	@Test
	void validateSeatAvailability_WhenSeatNotFound_ShouldThrowException() {
		Long nonExistentSeatId = 999L;
		when(bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, nonExistentSeatId,
				Arrays.asList(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED))).thenReturn(false);
		when(seatRepository.findById(nonExistentSeatId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> seatAvailabilityService.validateSeatAvailability(SESSION_ID, nonExistentSeatId))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Seat not found");

		verify(seatRepository).findById(nonExistentSeatId);
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
		when(bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, SEAT_ID_1,
				Arrays.asList(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED))).thenReturn(false);

		boolean result = seatAvailabilityService.isSeatAvailableForSession(SESSION_ID, SEAT_ID_1);

		assertThat(result).isTrue();
		verify(bookedSeatRepository).existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, SEAT_ID_1,
				Arrays.asList(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED));
	}

	@Test
	void isSeatAvailableForSession_WhenNotAvailable() {
		when(bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, SEAT_ID_1,
				Arrays.asList(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED))).thenReturn(true);

		boolean result = seatAvailabilityService.isSeatAvailableForSession(SESSION_ID, SEAT_ID_1);

		assertThat(result).isFalse();
	}

	@Test
	void getAvailableSeatsCount_Success() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.countByHallId(HALL_ID)).thenReturn(100L);
		when(bookedSeatRepository.countBySessionIdAndStatusIn(SESSION_ID,
				Arrays.asList(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED))).thenReturn(25L);

		int result = seatAvailabilityService.getAvailableSeatsCount(SESSION_ID);

		assertThat(result).isEqualTo(75);
		verify(sessionRepository).findById(SESSION_ID);
		verify(seatRepository).countByHallId(HALL_ID);
		verify(bookedSeatRepository).countBySessionIdAndStatusIn(SESSION_ID,
				Arrays.asList(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED));
	}

	@Test
	void getAvailableSeatsCount_WhenSessionNotFound_ShouldThrowException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> seatAvailabilityService.getAvailableSeatsCount(SESSION_ID))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Session not found");

		verify(sessionRepository).findById(SESSION_ID);
	}

	@Test
	void getAvailableSeatsCount_WhenAllSeatsAvailable() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.countByHallId(HALL_ID)).thenReturn(50L);
		when(bookedSeatRepository.countBySessionIdAndStatusIn(SESSION_ID,
				Arrays.asList(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED))).thenReturn(0L);

		int result = seatAvailabilityService.getAvailableSeatsCount(SESSION_ID);

		assertThat(result).isEqualTo(50);
	}

	@Test
	void getAvailableSeatsCount_WhenAllSeatsBooked() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.countByHallId(HALL_ID)).thenReturn(30L);
		when(bookedSeatRepository.countBySessionIdAndStatusIn(SESSION_ID,
				Arrays.asList(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED))).thenReturn(30L);

		int result = seatAvailabilityService.getAvailableSeatsCount(SESSION_ID);

		assertThat(result).isEqualTo(0);
	}

	@Test
	void getSeatAvailability_WithOnlyPendingStatus() {
		List<Seat> allSeats = Arrays.asList(testSeat1, testSeat2);
		List<BookedSeat> bookedSeats = Arrays.asList(pendingSeat);
		List<TicketType> activeTicketTypes = Arrays.asList(adultTicketType);

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByHallId(HALL_ID)).thenReturn(allSeats);
		when(bookedSeatRepository.findBySessionIdAndStatusIn(SESSION_ID,
				Arrays.asList(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED))).thenReturn(bookedSeats);
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(activeTicketTypes);

		SeatAvailabilityResponse result = seatAvailabilityService.getSeatAvailability(SESSION_ID);

		SeatAvailabilityResponse.SeatInfo seat1Info = result.getSeats().stream()
				.filter(s -> s.getId().equals(SEAT_ID_1)).findFirst().orElseThrow();
		assertThat(seat1Info.getAvailable()).isFalse();
		assertThat(seat1Info.getTemporarilyReserved()).isTrue();

		SeatAvailabilityResponse.SeatInfo seat2Info = result.getSeats().stream()
				.filter(s -> s.getId().equals(SEAT_ID_2)).findFirst().orElseThrow();
		assertThat(seat2Info.getAvailable()).isTrue();
		assertThat(seat2Info.getTemporarilyReserved()).isFalse();
	}

	@Test
	void getSeatAvailability_WithOnlyConfirmedStatus() {
		List<Seat> allSeats = Arrays.asList(testSeat1, testSeat2);
		List<BookedSeat> bookedSeats = Arrays.asList(bookedSeat);
		List<TicketType> activeTicketTypes = Arrays.asList(adultTicketType);

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByHallId(HALL_ID)).thenReturn(allSeats);
		when(bookedSeatRepository.findBySessionIdAndStatusIn(SESSION_ID,
				Arrays.asList(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED))).thenReturn(bookedSeats);
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(activeTicketTypes);

		SeatAvailabilityResponse result = seatAvailabilityService.getSeatAvailability(SESSION_ID);

		SeatAvailabilityResponse.SeatInfo seat1Info = result.getSeats().stream()
				.filter(s -> s.getId().equals(SEAT_ID_1)).findFirst().orElseThrow();
		assertThat(seat1Info.getAvailable()).isTrue();
		assertThat(seat1Info.getTemporarilyReserved()).isFalse();

		SeatAvailabilityResponse.SeatInfo seat2Info = result.getSeats().stream()
				.filter(s -> s.getId().equals(SEAT_ID_2)).findFirst().orElseThrow();
		assertThat(seat2Info.getAvailable()).isFalse();
		assertThat(seat2Info.getTemporarilyReserved()).isFalse();
	}

	@Test
	void getSeatAvailability_WithNoActiveTicketTypes() {
		List<Seat> allSeats = Arrays.asList(testSeat1, testSeat2);
		List<BookedSeat> bookedSeats = Collections.emptyList();

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByHallId(HALL_ID)).thenReturn(allSeats);
		when(bookedSeatRepository.findBySessionIdAndStatusIn(SESSION_ID,
				Arrays.asList(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED))).thenReturn(bookedSeats);
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(Collections.emptyList());

		SeatAvailabilityResponse result = seatAvailabilityService.getSeatAvailability(SESSION_ID);

		SeatAvailabilityResponse.SeatInfo seat1Info = result.getSeats().get(0);
		assertThat(seat1Info.getTicketPrices()).isEmpty();
		assertThat(seat1Info.getAvailable()).isTrue();
	}

	@Test
	void calculateSeatPrice_WithDifferentMultipliers() {
		SeatType vipSeatType = SeatType.VIP;
		BigDecimal vipMultiplier = vipSeatType.getPriceMultiplier();

		TicketType specialTicket = new TicketType();
		specialTicket.setPriceMultiplier(new BigDecimal("1.5"));
		specialTicket.setActive(true);

		BigDecimal result = seatAvailabilityService.calculateSeatPrice(testSession, testSeat2, specialTicket);

		BigDecimal expected = BASE_PRICE.multiply(vipMultiplier).multiply(new BigDecimal("1.5"));

		assertThat(result).isEqualByComparingTo(expected);
	}

	@Test
	void validateSeatAvailability_WithDifferentStatuses() {
		when(bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, SEAT_ID_1,
				Arrays.asList(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED))).thenReturn(true);

		assertThatThrownBy(() -> seatAvailabilityService.validateSeatAvailability(SESSION_ID, SEAT_ID_1))
				.isInstanceOf(SeatNotAvailableException.class);
	}

	@Test
	void getSeatAvailability_TransactionalReadOnly() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByHallId(HALL_ID)).thenReturn(Arrays.asList(testSeat1));
		when(bookedSeatRepository.findBySessionIdAndStatusIn(any(), any())).thenReturn(Collections.emptyList());
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(Arrays.asList(adultTicketType));

		SeatAvailabilityResponse result = seatAvailabilityService.getSeatAvailability(SESSION_ID);

		assertThat(result).isNotNull();
	}

	private void verifyPriceCalculations(SeatAvailabilityResponse.SeatInfo seatInfo) {
		seatInfo.getTicketPrices().forEach(priceInfo -> {
			if (priceInfo.getTicketTypeId().equals(1L)) {
				BigDecimal expectedAdultPrice = BASE_PRICE.multiply(testSeat1.getSeatType().getPriceMultiplier())
						.multiply(adultTicketType.getPriceMultiplier());
				assertThat(priceInfo.getFinalPrice()).isEqualByComparingTo(expectedAdultPrice);
			} else if (priceInfo.getTicketTypeId().equals(2L)) {
				BigDecimal expectedChildPrice = BASE_PRICE.multiply(testSeat1.getSeatType().getPriceMultiplier())
						.multiply(childTicketType.getPriceMultiplier());
				assertThat(priceInfo.getFinalPrice()).isEqualByComparingTo(expectedChildPrice);
			}
		});
	}
}