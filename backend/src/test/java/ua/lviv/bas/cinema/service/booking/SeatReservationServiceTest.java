package ua.lviv.bas.cinema.service.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import org.springframework.test.util.ReflectionTestUtils;

import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.booking.status.ReservationStatus;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.enums.SeatType;
import ua.lviv.bas.cinema.domain.ticket.TicketType;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.booking.response.SeatReservationResponse;
import ua.lviv.bas.cinema.exception.domain.booking.SeatNotAvailableException;
import ua.lviv.bas.cinema.exception.domain.cinema.SeatNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.mapper.booking.SeatReservationMapper;
import ua.lviv.bas.cinema.repository.booking.SeatReservationRepository;
import ua.lviv.bas.cinema.repository.cinema.SeatRepository;
import ua.lviv.bas.cinema.repository.cinema.SessionRepository;
import ua.lviv.bas.cinema.repository.ticket.TicketTypeRepository;
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
	private SeatReservationMapper seatReservationMapper;
	@Captor
	private ArgumentCaptor<SeatReservation> seatReservationCaptor;
	@InjectMocks
	private SeatReservationService seatReservationService;

	private Session testSession;
	private Seat testSeat;
	private Seat inactiveSeat;
	private User testUser;
	private TicketType ticketType;
	private CinemaHall hall;

	private static final Long SESSION_ID = 1L;
	private static final Long SEAT_ID = 3L;
	private static final Long USER_ID = 10L;
	private static final Long HALL_ID = 2L;
	private static final BigDecimal BASE_PRICE = new BigDecimal("200.00");
	private static final int TEMP_HOLD_MINUTES = 5;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(seatReservationService, "tempHoldMinutes", TEMP_HOLD_MINUTES);

		Movie movie = Movie.builder().id(1L).title("Test Movie").build();

		hall = CinemaHall.builder().id(HALL_ID).name("Hall A").build();

		testSession = Session.builder().id(SESSION_ID).movie(movie).hall(hall).basePrice(BASE_PRICE).build();

		testSeat = Seat.builder().id(SEAT_ID).row(1).number(1).seatType(SeatType.STANDARD).active(true).hall(hall)
				.build();

		inactiveSeat = Seat.builder().id(SEAT_ID).row(1).number(1).seatType(SeatType.STANDARD).active(false).hall(hall)
				.build();

		testUser = User.builder().id(USER_ID).build();

		ticketType = TicketType.builder().id(1L).displayName("Adult").active(true).build();
	}

	@Test
	void temporaryHoldSeat_Success() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByIdWithLock(SEAT_ID)).thenReturn(Optional.of(testSeat));
		when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(testSeat));
		when(seatReservationRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, SEAT_ID,
				ReservationStatus.ACTIVE_STATUSES)).thenReturn(false);

		seatReservationService.temporaryHoldSeat(SESSION_ID, SEAT_ID, testUser);

		verify(seatReservationRepository).save(seatReservationCaptor.capture());

		SeatReservation saved = seatReservationCaptor.getValue();
		assertThat(saved.getSeat()).isEqualTo(testSeat);
		assertThat(saved.getSession()).isEqualTo(testSession);
		assertThat(saved.getStatus()).isEqualTo(ReservationStatus.PENDING);
		assertThat(saved.getReservedByUser()).isEqualTo(testUser);
		assertThat(saved.getReservedUntil()).isAfter(LocalDateTime.now());
	}

	@Test
	void temporaryHoldSeat_WhenSessionNotFound_ThrowsException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> seatReservationService.temporaryHoldSeat(SESSION_ID, SEAT_ID, testUser))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void temporaryHoldSeat_WhenSeatNotFound_ThrowsException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByIdWithLock(SEAT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> seatReservationService.temporaryHoldSeat(SESSION_ID, SEAT_ID, testUser))
				.isInstanceOf(SeatNotFoundException.class);
	}

	@Test
	void temporaryHoldSeat_WhenSeatInactive_ThrowsException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByIdWithLock(SEAT_ID)).thenReturn(Optional.of(inactiveSeat));
		when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(inactiveSeat));

		assertThatThrownBy(() -> seatReservationService.temporaryHoldSeat(SESSION_ID, SEAT_ID, testUser))
				.isInstanceOf(SeatNotAvailableException.class);
	}

	@Test
	void temporaryHoldSeat_WhenSeatAlreadyReserved_ThrowsException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByIdWithLock(SEAT_ID)).thenReturn(Optional.of(testSeat));
		when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(testSeat));
		when(seatReservationRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, SEAT_ID,
				ReservationStatus.ACTIVE_STATUSES)).thenReturn(true);

		assertThatThrownBy(() -> seatReservationService.temporaryHoldSeat(SESSION_ID, SEAT_ID, testUser))
				.isInstanceOf(SeatNotAvailableException.class);
	}

	@Test
	void cancelTemporaryHold_Success() {
		SeatReservation reservation = SeatReservation.builder().id(1L).seat(testSeat).session(testSession)
				.status(ReservationStatus.PENDING).reservedByUser(testUser).build();

		when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID,
				ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.of(reservation));

		seatReservationService.cancelTemporaryHold(SESSION_ID, SEAT_ID, testUser);

		verify(seatReservationRepository).delete(reservation);
	}

	@Test
	void cancelTemporaryHold_WhenNoActiveHold_ThrowsException() {
		when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID,
				ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> seatReservationService.cancelTemporaryHold(SESSION_ID, SEAT_ID, testUser))
				.isInstanceOf(SeatNotAvailableException.class);
	}

	@Test
	void validateSeatAvailability_Success() {
		when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(testSeat));
		when(seatReservationRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, SEAT_ID,
				ReservationStatus.ACTIVE_STATUSES)).thenReturn(false);

		seatReservationService.validateSeatAvailability(SESSION_ID, SEAT_ID);
	}

	@Test
	void validateSeatAvailability_WhenSeatNotFound_ThrowsException() {
		when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> seatReservationService.validateSeatAvailability(SESSION_ID, SEAT_ID))
				.isInstanceOf(SeatNotFoundException.class);
	}

	@Test
	void validateSeatAvailability_WhenSeatInactive_ThrowsException() {
		when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(inactiveSeat));

		assertThatThrownBy(() -> seatReservationService.validateSeatAvailability(SESSION_ID, SEAT_ID))
				.isInstanceOf(SeatNotAvailableException.class);
	}

	@Test
	void validateSeatAvailability_WhenSeatReserved_ThrowsException() {
		when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(testSeat));
		when(seatReservationRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, SEAT_ID,
				ReservationStatus.ACTIVE_STATUSES)).thenReturn(true);

		assertThatThrownBy(() -> seatReservationService.validateSeatAvailability(SESSION_ID, SEAT_ID))
				.isInstanceOf(SeatNotAvailableException.class);
	}

	@Test
	void isSeatAvailableForSession_WhenAvailable_ReturnsTrue() {
		when(seatReservationRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, SEAT_ID,
				ReservationStatus.ACTIVE_STATUSES)).thenReturn(false);

		boolean result = seatReservationService.isSeatAvailableForSession(SESSION_ID, SEAT_ID);

		assertThat(result).isTrue();
	}

	@Test
	void isSeatAvailableForSession_WhenNotAvailable_ReturnsFalse() {
		when(seatReservationRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, SEAT_ID,
				ReservationStatus.ACTIVE_STATUSES)).thenReturn(true);

		boolean result = seatReservationService.isSeatAvailableForSession(SESSION_ID, SEAT_ID);

		assertThat(result).isFalse();
	}

	@Test
	void getSeatAvailabilityStatus_WhenSeatAvailable_ReturnsAvailable() {
		when(seatReservationRepository.findStatusesBySessionIdAndSeatId(SESSION_ID, SEAT_ID))
				.thenReturn(Collections.emptyList());

		SeatReservationService.SeatAvailabilityCheck result = seatReservationService
				.getSeatAvailabilityStatus(SESSION_ID, SEAT_ID);

		assertThat(result.available()).isTrue();
		assertThat(result.status()).isNull();
		assertThat(result.isTemporarilyReserved()).isFalse();
		assertThat(result.isConfirmed()).isFalse();
	}

	@Test
	void getSeatAvailabilityStatus_WhenSeatConfirmed_ReturnsNotAvailable() {
		when(seatReservationRepository.findStatusesBySessionIdAndSeatId(SESSION_ID, SEAT_ID))
				.thenReturn(List.of(ReservationStatus.CONFIRMED));

		SeatReservationService.SeatAvailabilityCheck result = seatReservationService
				.getSeatAvailabilityStatus(SESSION_ID, SEAT_ID);

		assertThat(result.available()).isFalse();
		assertThat(result.status()).isEqualTo(ReservationStatus.CONFIRMED);
		assertThat(result.isTemporarilyReserved()).isFalse();
		assertThat(result.isConfirmed()).isTrue();
	}

	@Test
	void getSeatAvailabilityStatus_WhenSeatPending_ReturnsTemporarilyReserved() {
		when(seatReservationRepository.findStatusesBySessionIdAndSeatId(SESSION_ID, SEAT_ID))
				.thenReturn(List.of(ReservationStatus.PENDING));

		SeatReservationService.SeatAvailabilityCheck result = seatReservationService
				.getSeatAvailabilityStatus(SESSION_ID, SEAT_ID);

		assertThat(result.available()).isFalse();
		assertThat(result.status()).isEqualTo(ReservationStatus.PENDING);
		assertThat(result.isTemporarilyReserved()).isTrue();
		assertThat(result.isConfirmed()).isFalse();
	}

	@Test
	void getSeatAvailability_Success() {
		List<Seat> seats = Arrays.asList(testSeat);
		List<TicketType> ticketTypes = Arrays.asList(ticketType);
		List<Object[]> bookedSeatData = Collections.emptyList();

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByHallId(HALL_ID)).thenReturn(seats);
		when(seatReservationRepository.findBookedSeatIds(HALL_ID, SESSION_ID, ReservationStatus.ACTIVE_STATUSES))
				.thenReturn(bookedSeatData);
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(ticketTypes);
		when(priceCalculator.calculateSeatPrice(testSession, testSeat, ticketType)).thenReturn(BigDecimal.TEN);

		SeatReservationResponse.TicketPriceInfo priceInfo = new SeatReservationResponse.TicketPriceInfo(1L, "Adult",
				BigDecimal.TEN);
		SeatReservationResponse.SeatInfo seatInfo = new SeatReservationResponse.SeatInfo(SEAT_ID, 1, 1,
				SeatType.STANDARD, true, false, true, List.of(priceInfo));

		when(seatReservationMapper.toTicketPriceInfo(ticketType, BigDecimal.TEN)).thenReturn(priceInfo);
		when(seatReservationMapper.toSeatInfo(testSeat, true, false, List.of(priceInfo))).thenReturn(seatInfo);

		SeatReservationResponse expected = new SeatReservationResponse(SESSION_ID, "Test Movie", BASE_PRICE, "Hall A",
				1, List.of(seatInfo));
		when(seatReservationMapper.toResponse(testSession, List.of(seatInfo), 1)).thenReturn(expected);

		SeatReservationResponse result = seatReservationService.getSeatAvailability(SESSION_ID);

		assertThat(result).isNotNull();
		assertThat(result.sessionId()).isEqualTo(SESSION_ID);
		assertThat(result.availableSeats()).isEqualTo(1);
	}

	@Test
	void getSeatAvailability_WhenSessionNotFound_ThrowsException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> seatReservationService.getSeatAvailability(SESSION_ID))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void getAvailableSeatsCount_Success() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.countByHallId(HALL_ID)).thenReturn(100L);
		when(seatReservationRepository.countBySessionIdAndStatusIn(SESSION_ID, ReservationStatus.ACTIVE_STATUSES))
				.thenReturn(25L);

		int result = seatReservationService.getAvailableSeatsCount(SESSION_ID);

		assertThat(result).isEqualTo(75);
	}

	@Test
	void getAvailableSeatsCount_WhenSessionNotFound_ThrowsException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> seatReservationService.getAvailableSeatsCount(SESSION_ID))
				.isInstanceOf(SessionNotFoundException.class);
	}
}