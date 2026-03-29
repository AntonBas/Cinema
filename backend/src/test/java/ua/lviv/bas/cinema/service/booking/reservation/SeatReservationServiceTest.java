package ua.lviv.bas.cinema.service.booking.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import ua.lviv.bas.cinema.mapper.SeatReservationMapper;
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
	private ReservationValidator availabilityValidator;
	@Mock
	private SeatReservationMapper seatReservationMapper;
	@Captor
	private ArgumentCaptor<SeatReservation> seatReservationCaptor;
	@InjectMocks
	private SeatReservationService seatReservationService;

	private Session testSession;
	private Seat testSeat;
	private User testUser;
	private TicketType ticketType;

	private static final Long SESSION_ID = 1L;
	private static final Long SEAT_ID = 3L;
	private static final Long USER_ID = 10L;
	private static final BigDecimal BASE_PRICE = new BigDecimal("200.00");

	@BeforeEach
	void setUp() {
		Movie movie = new Movie();
		movie.setTitle("Test Movie");

		CinemaHall hall = new CinemaHall();
		hall.setId(2L);
		hall.setName("Hall A");

		testSession = new Session();
		testSession.setId(SESSION_ID);
		testSession.setMovie(movie);
		testSession.setHall(hall);
		testSession.setBasePrice(BASE_PRICE);

		testSeat = new Seat();
		testSeat.setId(SEAT_ID);
		testSeat.setRow(1);
		testSeat.setNumber(1);
		testSeat.setSeatType(SeatType.STANDARD);
		testSeat.setActive(true);
		testSeat.setHall(hall);

		testUser = new User();
		testUser.setId(USER_ID);

		ticketType = new TicketType();
		ticketType.setId(1L);
		ticketType.setDisplayName("Adult");
		ticketType.setActive(true);
	}

	@Test
	void temporaryHoldSeat_Success() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByIdWithLock(SEAT_ID)).thenReturn(Optional.of(testSeat));

		seatReservationService.temporaryHoldSeat(SESSION_ID, SEAT_ID, testUser);

		verify(availabilityValidator).validateSeat(SESSION_ID, SEAT_ID);
		verify(seatReservationRepository).save(seatReservationCaptor.capture());

		SeatReservation saved = seatReservationCaptor.getValue();
		assertThat(saved.getSeat()).isEqualTo(testSeat);
		assertThat(saved.getSession()).isEqualTo(testSession);
		assertThat(saved.getTicketType()).isNull();
		assertThat(saved.getSeatPrice()).isNull();
		assertThat(saved.getStatus()).isEqualTo(ReservationStatus.PENDING);
		assertThat(saved.getReservedByUser()).isEqualTo(testUser);
		assertThat(saved.getReservedUntil()).isAfter(saved.getReservedAt());
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
	void temporaryHoldSeat_WhenSeatNotAvailable_ThrowsException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByIdWithLock(SEAT_ID)).thenReturn(Optional.of(testSeat));
		doThrow(SeatNotAvailableException.forSeatAndSession(SEAT_ID, SESSION_ID)).when(availabilityValidator)
				.validateSeat(SESSION_ID, SEAT_ID);

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
	void getSeatAvailability_Success() {
		List<Seat> seats = Arrays.asList(testSeat);
		List<TicketType> ticketTypes = Arrays.asList(ticketType);
		List<Object[]> bookedSeatData = Collections.emptyList();

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByHallId(testSession.getHall().getId())).thenReturn(seats);
		when(seatReservationRepository.findBookedSeatIds(any(), any(), any())).thenReturn(bookedSeatData);
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(ticketTypes);
		when(priceCalculator.calculateSeatPrice(any(), any(), any())).thenReturn(BigDecimal.TEN);

		SeatReservationResponse.TicketPriceInfo priceInfo = new SeatReservationResponse.TicketPriceInfo(1L, "Adult",
				BigDecimal.TEN);
		SeatReservationResponse.SeatInfo seatInfo = new SeatReservationResponse.SeatInfo(SEAT_ID, 1, 1,
				SeatType.STANDARD, true, false, true, List.of(priceInfo));

		when(seatReservationMapper.toTicketPriceInfo(any(), any())).thenReturn(priceInfo);
		when(seatReservationMapper.toSeatInfo(any(), anyBoolean(), anyBoolean(), any())).thenReturn(seatInfo);

		SeatReservationResponse expected = new SeatReservationResponse(SESSION_ID, "Test Movie", BASE_PRICE, "Hall A",
				1, List.of(seatInfo));
		when(seatReservationMapper.toResponse(any(), any(), anyInt())).thenReturn(expected);

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
	void getSeatAvailability_WithBookedSeats_FiltersNullStatus() {
		List<Seat> seats = Arrays.asList(testSeat);
		List<TicketType> ticketTypes = Arrays.asList(ticketType);

		List<Object[]> bookedSeatData = new ArrayList<>();
		bookedSeatData.add(new Object[] { SEAT_ID, null });

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findByHallId(testSession.getHall().getId())).thenReturn(seats);
		when(seatReservationRepository.findBookedSeatIds(any(), any(), any())).thenReturn(bookedSeatData);
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(ticketTypes);
		when(priceCalculator.calculateSeatPrice(any(), any(), any())).thenReturn(BigDecimal.TEN);

		SeatReservationResponse.TicketPriceInfo priceInfo = new SeatReservationResponse.TicketPriceInfo(1L, "Adult",
				BigDecimal.TEN);
		SeatReservationResponse.SeatInfo seatInfo = new SeatReservationResponse.SeatInfo(SEAT_ID, 1, 1,
				SeatType.STANDARD, true, false, true, List.of(priceInfo));

		when(seatReservationMapper.toTicketPriceInfo(any(), any())).thenReturn(priceInfo);
		when(seatReservationMapper.toSeatInfo(any(), anyBoolean(), anyBoolean(), any())).thenReturn(seatInfo);

		SeatReservationResponse expected = new SeatReservationResponse(SESSION_ID, "Test Movie", BASE_PRICE, "Hall A",
				1, List.of(seatInfo));
		when(seatReservationMapper.toResponse(any(), any(), anyInt())).thenReturn(expected);

		SeatReservationResponse result = seatReservationService.getSeatAvailability(SESSION_ID);

		assertThat(result).isNotNull();
		assertThat(result.availableSeats()).isEqualTo(1);
	}

	@Test
	void getAvailableSeatsCount_Success() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.countByHallId(testSession.getHall().getId())).thenReturn(100L);
		when(seatReservationRepository.countBySessionIdAndStatusIn(eq(SESSION_ID),
				eq(ReservationStatus.ACTIVE_STATUSES))).thenReturn(25L);

		int result = seatReservationService.getAvailableSeatsCount(SESSION_ID);
		assertThat(result).isEqualTo(75);
	}

	@Test
	void validateSeatAvailability_Success() {
		seatReservationService.validateSeatAvailability(SESSION_ID, SEAT_ID);
		verify(availabilityValidator).validateSeat(SESSION_ID, SEAT_ID);
	}

	@Test
	void isSeatAvailableForSession_WhenAvailable() {
		when(availabilityValidator.isSeatAvailable(SESSION_ID, SEAT_ID)).thenReturn(true);
		assertThat(seatReservationService.isSeatAvailableForSession(SESSION_ID, SEAT_ID)).isTrue();
	}

	@Test
	void isSeatAvailableForSession_WhenNotAvailable() {
		when(availabilityValidator.isSeatAvailable(SESSION_ID, SEAT_ID)).thenReturn(false);
		assertThat(seatReservationService.isSeatAvailableForSession(SESSION_ID, SEAT_ID)).isFalse();
	}
}