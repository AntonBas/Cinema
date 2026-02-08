package ua.lviv.bas.cinema.service.booking.availability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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

import ua.lviv.bas.cinema.domain.SeatReservation;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.enums.ReservationStatus;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatReservationResponse;
import ua.lviv.bas.cinema.exception.domain.booking.SeatNotAvailableException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.repository.SeatReservationRepository;
import ua.lviv.bas.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.repository.TicketTypeRepository;
import ua.lviv.bas.cinema.service.shared.PriceCalculatorService;

@ExtendWith(MockitoExtension.class)
public class SeatReservationServiceTest {

	@Mock
	private SessionRepository sessionRepository;

	@Mock
	private SeatRepository seatRepository;

	@Mock
	private SeatReservationRepository bookedSeatRepository;

	@Mock
	private TicketTypeRepository ticketTypeRepository;

	@Mock
	private PriceCalculatorService priceCalculator;

	@Mock
	private AvailabilityValidator availabilityValidator;

	@InjectMocks
	private SeatReservationService seatAvailabilityService;

	private Session testSession;
	private CinemaHall testHall;
	private Seat testSeat1;
	private Seat testSeat2;
	private Seat inactiveSeat;
	private TicketType adultTicketType;

	private static final Long SESSION_ID = 1L;
	private static final Long HALL_ID = 2L;
	private static final Long SEAT_ID_1 = 3L;
	private static final Long SEAT_ID_2 = 4L;
	private static final Long INACTIVE_SEAT_ID = 5L;
	private static final BigDecimal BASE_PRICE = new BigDecimal("200.00");

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
		adultTicketType.setActive(true);
	}

	@Test
	void getSeatAvailability_Success() {
		List<Seat> allSeats = Arrays.asList(testSeat1, testSeat2, inactiveSeat);
		List<TicketType> activeTicketTypes = Arrays.asList(adultTicketType);

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByHallId(HALL_ID)).thenReturn(allSeats);
		when(bookedSeatRepository.findBySessionIdAndStatusIn(eq(SESSION_ID), any()))
				.thenReturn(Collections.emptyList());
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(activeTicketTypes);
		when(priceCalculator.calculateSeatPrice(any(), any(), any())).thenReturn(BigDecimal.TEN);

		SeatReservationResponse result = seatAvailabilityService.getSeatAvailability(SESSION_ID);

		assertThat(result).isNotNull();
		assertThat(result.getSessionId()).isEqualTo(SESSION_ID);
		assertThat(result.getMovieTitle()).isEqualTo("Test Movie");
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
		SeatReservation bookedSeat = new SeatReservation();
		bookedSeat.setSeat(testSeat1);
		bookedSeat.setStatus(ReservationStatus.CONFIRMED);
		List<TicketType> activeTicketTypes = Arrays.asList(adultTicketType);

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByHallId(HALL_ID)).thenReturn(allSeats);
		when(bookedSeatRepository.findBySessionIdAndStatusIn(eq(SESSION_ID), any()))
				.thenReturn(Arrays.asList(bookedSeat));
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(activeTicketTypes);
		when(priceCalculator.calculateSeatPrice(any(), any(), any())).thenReturn(BigDecimal.TEN);

		SeatReservationResponse result = seatAvailabilityService.getSeatAvailability(SESSION_ID);

		assertThat(result.getAvailableSeats()).isEqualTo(1);
	}

	@Test
	void getSeatAvailability_WithTemporarilyReservedSeat() {
		List<Seat> allSeats = Arrays.asList(testSeat1, testSeat2);
		SeatReservation pendingSeat = new SeatReservation();
		pendingSeat.setSeat(testSeat1);
		pendingSeat.setStatus(ReservationStatus.PENDING);
		List<TicketType> activeTicketTypes = Arrays.asList(adultTicketType);

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByHallId(HALL_ID)).thenReturn(allSeats);
		when(bookedSeatRepository.findBySessionIdAndStatusIn(eq(SESSION_ID), any()))
				.thenReturn(Arrays.asList(pendingSeat));
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(activeTicketTypes);
		when(priceCalculator.calculateSeatPrice(any(), any(), any())).thenReturn(BigDecimal.TEN);

		SeatReservationResponse result = seatAvailabilityService.getSeatAvailability(SESSION_ID);

		assertThat(result.getAvailableSeats()).isEqualTo(1);
	}

	@Test
	void validateSeatAvailability_Success() {
		seatAvailabilityService.validateSeatAvailability(SESSION_ID, SEAT_ID_1);

		verify(availabilityValidator).validateSeat(SESSION_ID, SEAT_ID_1);
	}

	@Test
	void validateSeatAvailability_WhenValidatorThrowsException_ShouldPropagate() {
		doThrow(SeatNotAvailableException.seatInactive(SEAT_ID_1)).when(availabilityValidator).validateSeat(SESSION_ID,
				SEAT_ID_1);

		assertThatThrownBy(() -> seatAvailabilityService.validateSeatAvailability(SESSION_ID, SEAT_ID_1))
				.isInstanceOf(SeatNotAvailableException.class);
	}

	@Test
	void isSeatAvailableForSession_WhenAvailable() {
		when(availabilityValidator.isSeatAvailable(SESSION_ID, SEAT_ID_1)).thenReturn(true);

		boolean result = seatAvailabilityService.isSeatAvailableForSession(SESSION_ID, SEAT_ID_1);

		assertThat(result).isTrue();
	}

	@Test
	void isSeatAvailableForSession_WhenNotAvailable() {
		when(availabilityValidator.isSeatAvailable(SESSION_ID, SEAT_ID_1)).thenReturn(false);

		boolean result = seatAvailabilityService.isSeatAvailableForSession(SESSION_ID, SEAT_ID_1);

		assertThat(result).isFalse();
	}

	@Test
	void getAvailableSeatsCount_Success() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.countByHallId(HALL_ID)).thenReturn(100L);
		when(bookedSeatRepository.countBySessionIdAndStatusIn(eq(SESSION_ID), any())).thenReturn(25L);

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
	void getSeatAvailability_WithNoActiveTicketTypes() {
		List<Seat> allSeats = Arrays.asList(testSeat1);

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByHallId(HALL_ID)).thenReturn(allSeats);
		when(bookedSeatRepository.findBySessionIdAndStatusIn(eq(SESSION_ID), any()))
				.thenReturn(Collections.emptyList());
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(Collections.emptyList());

		SeatReservationResponse result = seatAvailabilityService.getSeatAvailability(SESSION_ID);

		assertThat(result.getSeats().get(0).getTicketPrices()).isEmpty();
	}

	@Test
	void getAvailableSeatsCount_WhenAllSeatsAvailable() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.countByHallId(HALL_ID)).thenReturn(50L);
		when(bookedSeatRepository.countBySessionIdAndStatusIn(eq(SESSION_ID), any())).thenReturn(0L);

		int result = seatAvailabilityService.getAvailableSeatsCount(SESSION_ID);

		assertThat(result).isEqualTo(50);
	}

	@Test
	void getAvailableSeatsCount_WhenAllSeatsBooked() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.countByHallId(HALL_ID)).thenReturn(30L);
		when(bookedSeatRepository.countBySessionIdAndStatusIn(eq(SESSION_ID), any())).thenReturn(30L);

		int result = seatAvailabilityService.getAvailableSeatsCount(SESSION_ID);

		assertThat(result).isEqualTo(0);
	}
}