package ua.lviv.bas.cinema.service.booking.reservation;

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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.SeatReservation;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.ReservationStatus;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.booking.response.SeatReservationResponse;
import ua.lviv.bas.cinema.exception.domain.booking.SeatNotAvailableException;
import ua.lviv.bas.cinema.exception.domain.cinema.SeatNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.mapper.SeatReservationMapper;
import ua.lviv.bas.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.repository.SeatReservationRepository;
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
	private SeatReservationRepository seatReservationRepository;

	@Mock
	private TicketTypeRepository ticketTypeRepository;

	@Mock
	private PriceCalculatorService priceCalculator;

	@Mock
	private ReservationValidator availabilityValidator;

	@Mock
	private SeatReservationMapper seatReservationMapper;

	@Captor
	private ArgumentCaptor<SeatReservation> seatReservationCaptor;

	@InjectMocks
	private SeatReservationService seatReservationService;

	private Session testSession;
	private CinemaHall testHall;
	private Seat testSeat1;
	private Seat testSeat2;
	private Seat inactiveSeat;
	private TicketType adultTicketType;
	private SeatReservationResponse.SeatInfo seatInfo1;
	private SeatReservationResponse.SeatInfo seatInfo2;
	private SeatReservationResponse.SeatInfo inactiveSeatInfo;
	private SeatReservationResponse.TicketPriceInfo ticketPriceInfo;
	private User testUser;

	private static final Long SESSION_ID = 1L;
	private static final Long HALL_ID = 2L;
	private static final Long SEAT_ID_1 = 3L;
	private static final Long SEAT_ID_2 = 4L;
	private static final Long INACTIVE_SEAT_ID = 5L;
	private static final Long USER_ID = 10L;
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

		testUser = new User();
		testUser.setId(USER_ID);
		testUser.setEmail("test@example.com");

		ticketPriceInfo = new SeatReservationResponse.TicketPriceInfo(1L, "Adult", BigDecimal.TEN);
	}

	@Test
	void temporaryHoldSeat_Success() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findById(SEAT_ID_1)).thenReturn(Optional.of(testSeat1));

		seatReservationService.temporaryHoldSeat(SESSION_ID, SEAT_ID_1, testUser);

		verify(availabilityValidator).validateSeat(SESSION_ID, SEAT_ID_1);
		verify(seatReservationRepository).save(seatReservationCaptor.capture());

		SeatReservation savedReservation = seatReservationCaptor.getValue();

		assertThat(savedReservation.getSeat()).isEqualTo(testSeat1);
		assertThat(savedReservation.getSession()).isEqualTo(testSession);
		assertThat(savedReservation.getTicketType()).isNull();
		assertThat(savedReservation.getSeatPrice()).isNull();
		assertThat(savedReservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
		assertThat(savedReservation.getReservedByUser()).isEqualTo(testUser);
		assertThat(savedReservation.getReservedAt()).isNotNull();
		assertThat(savedReservation.getReservedUntil()).isNotNull();
		assertThat(savedReservation.getReservedUntil()).isAfter(savedReservation.getReservedAt());
	}

	@Test
	void temporaryHoldSeat_WhenSessionNotFound_ShouldThrowException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> seatReservationService.temporaryHoldSeat(SESSION_ID, SEAT_ID_1, testUser))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void temporaryHoldSeat_WhenSeatNotFound_ShouldThrowException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findById(SEAT_ID_1)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> seatReservationService.temporaryHoldSeat(SESSION_ID, SEAT_ID_1, testUser))
				.isInstanceOf(SeatNotFoundException.class);
	}

	@Test
	void temporaryHoldSeat_WhenSeatNotAvailable_ShouldThrowException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findById(SEAT_ID_1)).thenReturn(Optional.of(testSeat1));
		doThrow(SeatNotAvailableException.forSeatAndSession(SEAT_ID_1, SESSION_ID)).when(availabilityValidator)
				.validateSeat(SESSION_ID, SEAT_ID_1);

		assertThatThrownBy(() -> seatReservationService.temporaryHoldSeat(SESSION_ID, SEAT_ID_1, testUser))
				.isInstanceOf(SeatNotAvailableException.class);
	}

	@Test
	void getSeatAvailability_Success() {
		List<Seat> allSeats = Arrays.asList(testSeat1, testSeat2, inactiveSeat);
		List<TicketType> activeTicketTypes = Arrays.asList(adultTicketType);
		List<Object[]> bookedSeatData = Collections.emptyList();

		when(availabilityValidator.getSeatAvailabilityStatus(eq(SESSION_ID), eq(SEAT_ID_1)))
				.thenReturn(new ReservationValidator.SeatAvailabilityCheck(true, null));
		when(availabilityValidator.getSeatAvailabilityStatus(eq(SESSION_ID), eq(SEAT_ID_2)))
				.thenReturn(new ReservationValidator.SeatAvailabilityCheck(true, null));
		when(availabilityValidator.getSeatAvailabilityStatus(eq(SESSION_ID), eq(INACTIVE_SEAT_ID)))
				.thenReturn(new ReservationValidator.SeatAvailabilityCheck(true, null));

		seatInfo1 = new SeatReservationResponse.SeatInfo(SEAT_ID_1, 1, 1, SeatType.STANDARD, true, false, true,
				List.of(ticketPriceInfo));

		seatInfo2 = new SeatReservationResponse.SeatInfo(SEAT_ID_2, 1, 2, SeatType.VIP, true, false, true,
				List.of(ticketPriceInfo));

		inactiveSeatInfo = new SeatReservationResponse.SeatInfo(INACTIVE_SEAT_ID, 2, 1, SeatType.STANDARD, false, false,
				false, List.of(ticketPriceInfo));

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByHallId(HALL_ID)).thenReturn(allSeats);
		when(seatReservationRepository.findBookedSeatIds(eq(HALL_ID), eq(SESSION_ID), any()))
				.thenReturn(bookedSeatData);
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(activeTicketTypes);
		when(priceCalculator.calculateSeatPrice(any(), any(), any())).thenReturn(BigDecimal.TEN);

		when(seatReservationMapper.toTicketPriceInfo(adultTicketType, BigDecimal.TEN)).thenReturn(ticketPriceInfo);
		when(seatReservationMapper.toSeatInfo(testSeat1, true, false, List.of(ticketPriceInfo))).thenReturn(seatInfo1);
		when(seatReservationMapper.toSeatInfo(testSeat2, true, false, List.of(ticketPriceInfo))).thenReturn(seatInfo2);
		when(seatReservationMapper.toSeatInfo(inactiveSeat, false, false, List.of(ticketPriceInfo)))
				.thenReturn(inactiveSeatInfo);

		SeatReservationResponse expectedResponse = new SeatReservationResponse(SESSION_ID, "Test Movie", BASE_PRICE,
				"Hall A", 2, List.of(seatInfo1, seatInfo2, inactiveSeatInfo));

		when(seatReservationMapper.toResponse(testSession, List.of(seatInfo1, seatInfo2, inactiveSeatInfo), 2))
				.thenReturn(expectedResponse);

		SeatReservationResponse result = seatReservationService.getSeatAvailability(SESSION_ID);

		assertThat(result).isNotNull();
		assertThat(result.sessionId()).isEqualTo(SESSION_ID);
		assertThat(result.movieTitle()).isEqualTo("Test Movie");
		assertThat(result.availableSeats()).isEqualTo(2);
		assertThat(result.seats()).hasSize(3);
	}

	@Test
	void getSeatAvailability_WhenSessionNotFound_ShouldThrowException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> seatReservationService.getSeatAvailability(SESSION_ID))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void getSeatAvailability_WithBookedSeats() {
		List<Seat> allSeats = Arrays.asList(testSeat1, testSeat2);
		List<TicketType> activeTicketTypes = Arrays.asList(adultTicketType);

		Object[] bookedSeat1 = new Object[] { SEAT_ID_1, true };
		List<Object[]> bookedSeatData = Collections.singletonList(bookedSeat1);

		when(availabilityValidator.getSeatAvailabilityStatus(eq(SESSION_ID), eq(SEAT_ID_1)))
				.thenReturn(new ReservationValidator.SeatAvailabilityCheck(false, ReservationStatus.CONFIRMED));
		when(availabilityValidator.getSeatAvailabilityStatus(eq(SESSION_ID), eq(SEAT_ID_2)))
				.thenReturn(new ReservationValidator.SeatAvailabilityCheck(true, null));

		SeatReservationResponse.SeatInfo bookedSeatInfo = new SeatReservationResponse.SeatInfo(SEAT_ID_1, 1, 1,
				SeatType.STANDARD, false, false, true, List.of(ticketPriceInfo));

		SeatReservationResponse.SeatInfo availableSeatInfo = new SeatReservationResponse.SeatInfo(SEAT_ID_2, 1, 2,
				SeatType.VIP, true, false, true, List.of(ticketPriceInfo));

		SeatReservationResponse expectedResponse = new SeatReservationResponse(SESSION_ID, "Test Movie", BASE_PRICE,
				"Hall A", 1, List.of(bookedSeatInfo, availableSeatInfo));

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByHallId(HALL_ID)).thenReturn(allSeats);
		when(seatReservationRepository.findBookedSeatIds(eq(HALL_ID), eq(SESSION_ID), any()))
				.thenReturn(bookedSeatData);
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(activeTicketTypes);
		when(priceCalculator.calculateSeatPrice(any(), any(), any())).thenReturn(BigDecimal.TEN);

		when(seatReservationMapper.toTicketPriceInfo(adultTicketType, BigDecimal.TEN)).thenReturn(ticketPriceInfo);
		when(seatReservationMapper.toSeatInfo(testSeat1, false, false, List.of(ticketPriceInfo)))
				.thenReturn(bookedSeatInfo);
		when(seatReservationMapper.toSeatInfo(testSeat2, true, false, List.of(ticketPriceInfo)))
				.thenReturn(availableSeatInfo);
		when(seatReservationMapper.toResponse(testSession, List.of(bookedSeatInfo, availableSeatInfo), 1))
				.thenReturn(expectedResponse);

		SeatReservationResponse result = seatReservationService.getSeatAvailability(SESSION_ID);

		assertThat(result).isNotNull();
		assertThat(result.availableSeats()).isEqualTo(1);
	}

	@Test
	void getSeatAvailability_WithTemporarilyReservedSeat() {
		List<Seat> allSeats = Arrays.asList(testSeat1, testSeat2);
		List<TicketType> activeTicketTypes = Arrays.asList(adultTicketType);

		List<Object[]> bookedSeatData = Collections.emptyList();

		when(availabilityValidator.getSeatAvailabilityStatus(eq(SESSION_ID), eq(SEAT_ID_1)))
				.thenReturn(new ReservationValidator.SeatAvailabilityCheck(false, ReservationStatus.PENDING));
		when(availabilityValidator.getSeatAvailabilityStatus(eq(SESSION_ID), eq(SEAT_ID_2)))
				.thenReturn(new ReservationValidator.SeatAvailabilityCheck(true, null));

		SeatReservationResponse.SeatInfo pendingSeatInfo = new SeatReservationResponse.SeatInfo(SEAT_ID_1, 1, 1,
				SeatType.STANDARD, false, true, true, List.of(ticketPriceInfo));

		SeatReservationResponse.SeatInfo availableSeatInfo = new SeatReservationResponse.SeatInfo(SEAT_ID_2, 1, 2,
				SeatType.VIP, true, false, true, List.of(ticketPriceInfo));

		SeatReservationResponse expectedResponse = new SeatReservationResponse(SESSION_ID, "Test Movie", BASE_PRICE,
				"Hall A", 1, List.of(pendingSeatInfo, availableSeatInfo));

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByHallId(HALL_ID)).thenReturn(allSeats);
		when(seatReservationRepository.findBookedSeatIds(eq(HALL_ID), eq(SESSION_ID), any()))
				.thenReturn(bookedSeatData);
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(activeTicketTypes);
		when(priceCalculator.calculateSeatPrice(any(), any(), any())).thenReturn(BigDecimal.TEN);

		when(seatReservationMapper.toTicketPriceInfo(adultTicketType, BigDecimal.TEN)).thenReturn(ticketPriceInfo);
		when(seatReservationMapper.toSeatInfo(testSeat1, false, true, List.of(ticketPriceInfo)))
				.thenReturn(pendingSeatInfo);
		when(seatReservationMapper.toSeatInfo(testSeat2, true, false, List.of(ticketPriceInfo)))
				.thenReturn(availableSeatInfo);
		when(seatReservationMapper.toResponse(testSession, List.of(pendingSeatInfo, availableSeatInfo), 1))
				.thenReturn(expectedResponse);

		SeatReservationResponse result = seatReservationService.getSeatAvailability(SESSION_ID);

		assertThat(result).isNotNull();
		assertThat(result.availableSeats()).isEqualTo(1);
	}

	@Test
	void validateSeatAvailability_Success() {
		seatReservationService.validateSeatAvailability(SESSION_ID, SEAT_ID_1);

		verify(availabilityValidator).validateSeat(SESSION_ID, SEAT_ID_1);
	}

	@Test
	void validateSeatAvailability_WhenValidatorThrowsException_ShouldPropagate() {
		doThrow(SeatNotAvailableException.seatInactive(SEAT_ID_1)).when(availabilityValidator).validateSeat(SESSION_ID,
				SEAT_ID_1);

		assertThatThrownBy(() -> seatReservationService.validateSeatAvailability(SESSION_ID, SEAT_ID_1))
				.isInstanceOf(SeatNotAvailableException.class);
	}

	@Test
	void isSeatAvailableForSession_WhenAvailable() {
		when(availabilityValidator.isSeatAvailable(SESSION_ID, SEAT_ID_1)).thenReturn(true);

		boolean result = seatReservationService.isSeatAvailableForSession(SESSION_ID, SEAT_ID_1);

		assertThat(result).isTrue();
	}

	@Test
	void isSeatAvailableForSession_WhenNotAvailable() {
		when(availabilityValidator.isSeatAvailable(SESSION_ID, SEAT_ID_1)).thenReturn(false);

		boolean result = seatReservationService.isSeatAvailableForSession(SESSION_ID, SEAT_ID_1);

		assertThat(result).isFalse();
	}

	@Test
	void getAvailableSeatsCount_Success() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.countByHallId(HALL_ID)).thenReturn(100L);
		when(seatReservationRepository.countBySessionIdAndStatusIn(eq(SESSION_ID), any())).thenReturn(25L);

		int result = seatReservationService.getAvailableSeatsCount(SESSION_ID);

		assertThat(result).isEqualTo(75);
	}

	@Test
	void getAvailableSeatsCount_WhenSessionNotFound_ShouldThrowException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> seatReservationService.getAvailableSeatsCount(SESSION_ID))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void getSeatAvailability_WithNoActiveTicketTypes() {
		List<Seat> allSeats = Arrays.asList(testSeat1);
		List<Object[]> bookedSeatData = Collections.emptyList();

		when(availabilityValidator.getSeatAvailabilityStatus(eq(SESSION_ID), eq(SEAT_ID_1)))
				.thenReturn(new ReservationValidator.SeatAvailabilityCheck(true, null));

		SeatReservationResponse.SeatInfo seatInfoWithNoPrices = new SeatReservationResponse.SeatInfo(SEAT_ID_1, 1, 1,
				SeatType.STANDARD, true, false, true, Collections.emptyList());

		SeatReservationResponse expectedResponse = new SeatReservationResponse(SESSION_ID, "Test Movie", BASE_PRICE,
				"Hall A", 1, List.of(seatInfoWithNoPrices));

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByHallId(HALL_ID)).thenReturn(allSeats);
		when(seatReservationRepository.findBookedSeatIds(eq(HALL_ID), eq(SESSION_ID), any()))
				.thenReturn(bookedSeatData);
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(Collections.emptyList());

		when(seatReservationMapper.toSeatInfo(testSeat1, true, false, Collections.emptyList()))
				.thenReturn(seatInfoWithNoPrices);
		when(seatReservationMapper.toResponse(testSession, List.of(seatInfoWithNoPrices), 1))
				.thenReturn(expectedResponse);

		SeatReservationResponse result = seatReservationService.getSeatAvailability(SESSION_ID);

		assertThat(result).isNotNull();
		assertThat(result.seats().get(0).ticketPrices()).isEmpty();
	}

	@Test
	void getAvailableSeatsCount_WhenAllSeatsAvailable() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.countByHallId(HALL_ID)).thenReturn(50L);
		when(seatReservationRepository.countBySessionIdAndStatusIn(eq(SESSION_ID), any())).thenReturn(0L);

		int result = seatReservationService.getAvailableSeatsCount(SESSION_ID);

		assertThat(result).isEqualTo(50);
	}

	@Test
	void getAvailableSeatsCount_WhenAllSeatsBooked() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.countByHallId(HALL_ID)).thenReturn(30L);
		when(seatReservationRepository.countBySessionIdAndStatusIn(eq(SESSION_ID), any())).thenReturn(30L);

		int result = seatReservationService.getAvailableSeatsCount(SESSION_ID);

		assertThat(result).isEqualTo(0);
	}
}