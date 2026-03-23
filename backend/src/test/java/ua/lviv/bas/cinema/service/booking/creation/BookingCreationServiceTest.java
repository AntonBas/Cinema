package ua.lviv.bas.cinema.service.booking.creation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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

import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.SeatReservation;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.enums.ReservationStatus;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.booking.request.BookingCreateRequest;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;
import ua.lviv.bas.cinema.exception.domain.booking.SeatNotAvailableException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeNotFoundException;
import ua.lviv.bas.cinema.mapper.BookingMapper;
import ua.lviv.bas.cinema.repository.BookingRepository;
import ua.lviv.bas.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.repository.SeatReservationRepository;
import ua.lviv.bas.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.repository.TicketTypeRepository;
import ua.lviv.bas.cinema.service.booking.reservation.ReservationValidator;
import ua.lviv.bas.cinema.service.shared.PriceCalculatorService;
import ua.lviv.bas.cinema.service.user.BonusService;

@ExtendWith(MockitoExtension.class)
public class BookingCreationServiceTest {

	@Mock
	private BookingRepository bookingRepository;

	@Mock
	private SessionRepository sessionRepository;

	@Mock
	private SeatRepository seatRepository;

	@Mock
	private TicketTypeRepository ticketTypeRepository;

	@Mock
	private SeatReservationRepository seatReservationRepository;

	@Mock
	private BookingMapper bookingMapper;

	@Mock
	private ReservationValidator availabilityValidator;

	@Mock
	private BonusService bonusService;

	@Mock
	private PriceCalculatorService priceCalculator;

	@Mock
	private BookingPriceCalculator bookingPriceCalculator;

	@Mock
	private BookingValidator bookingValidator;

	@InjectMocks
	private BookingCreationService bookingCreationService;

	@Captor
	private ArgumentCaptor<Booking> bookingCaptor;

	@Captor
	private ArgumentCaptor<List<SeatReservation>> seatReservationsCaptor;

	@Captor
	private ArgumentCaptor<SeatReservation> newReservationCaptor;

	private User testUser;
	private Session testSession;
	private Seat testSeat1;
	private Seat testSeat2;
	private TicketType adultTicketType;
	private TicketType childTicketType;
	private BookingCreateRequest createRequest;
	private Booking savedBooking;
	private BookingResponse bookingResponse;

	private static final Long USER_ID = 1L;
	private static final Long SESSION_ID = 2L;
	private static final Long SEAT_ID_1 = 3L;
	private static final Long SEAT_ID_2 = 4L;
	private static final Long TICKET_TYPE_ADULT_ID = 5L;
	private static final Long TICKET_TYPE_CHILD_ID = 6L;
	private static final Long BOOKING_ID = 10L;
	private static final BigDecimal BASE_PRICE = new BigDecimal("200.00");
	private static final BigDecimal SEAT_1_PRICE = new BigDecimal("200.00");
	private static final BigDecimal SEAT_2_PRICE = new BigDecimal("280.00");
	private static final BigDecimal TOTAL_PRICE = new BigDecimal("480.00");
	private static final BigDecimal DISCOUNT_AMOUNT = new BigDecimal("100.00");
	private static final BigDecimal FINAL_PRICE = new BigDecimal("380.00");
	private static final Integer BONUS_POINTS_USED = 100;
	private static final int EXPIRATION_MINUTES = 20;
	private static final int TEMP_HOLD_MINUTES = 5;
	private static final String BOOKING_NUMBER = "BK-2024-00001";

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(bookingCreationService, "expirationMinutes", EXPIRATION_MINUTES);
		ReflectionTestUtils.setField(bookingCreationService, "tempHoldMinutes", TEMP_HOLD_MINUTES);

		testUser = User.builder().id(USER_ID).email("test@example.com").build();

		Movie movie = Movie.builder().id(100L).title("Test Movie").build();

		CinemaHall hall = CinemaHall.builder().id(200L).name("Hall A").build();

		testSession = Session.builder().id(SESSION_ID).movie(movie).hall(hall).basePrice(BASE_PRICE)
				.status(CinemaSessionStatus.SCHEDULED).startTime(LocalDateTime.now().plusHours(2)).build();

		testSeat1 = Seat.builder().id(SEAT_ID_1).row(1).number(1).seatType(SeatType.STANDARD).active(true).build();

		testSeat2 = Seat.builder().id(SEAT_ID_2).row(1).number(2).seatType(SeatType.VIP).active(true).build();

		adultTicketType = TicketType.builder().id(TICKET_TYPE_ADULT_ID).displayName("Adult").build();

		childTicketType = TicketType.builder().id(TICKET_TYPE_CHILD_ID).displayName("Child").build();

		BookingCreateRequest.SeatSelectionRequest seatSelection1 = new BookingCreateRequest.SeatSelectionRequest(
				SEAT_ID_1, TICKET_TYPE_ADULT_ID);

		BookingCreateRequest.SeatSelectionRequest seatSelection2 = new BookingCreateRequest.SeatSelectionRequest(
				SEAT_ID_2, TICKET_TYPE_CHILD_ID);

		createRequest = new BookingCreateRequest(SESSION_ID, Arrays.asList(seatSelection1, seatSelection2),
				BONUS_POINTS_USED);

		bookingResponse = new BookingResponse(BOOKING_ID, BOOKING_NUMBER, BookingStatus.PENDING, SESSION_ID,
				LocalDateTime.now(), "Test Movie", "Hall A", TOTAL_PRICE, BONUS_POINTS_USED, DISCOUNT_AMOUNT,
				FINAL_PRICE, null, null, null, null);
	}

	@Test
	void createBooking_Success() {
		SeatReservation pendingReservation1 = SeatReservation.builder().id(1L).seat(testSeat1).session(testSession)
				.status(ReservationStatus.PENDING).reservedUntil(LocalDateTime.now().plusMinutes(TEMP_HOLD_MINUTES))
				.reservedByUser(testUser).build();

		SeatReservation pendingReservation2 = SeatReservation.builder().id(2L).seat(testSeat2).session(testSession)
				.status(ReservationStatus.PENDING).reservedUntil(LocalDateTime.now().plusMinutes(TEMP_HOLD_MINUTES))
				.reservedByUser(testUser).build();

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID_1,
				ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.of(pendingReservation1));
		when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID_2,
				ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.of(pendingReservation2));
		when(ticketTypeRepository.findById(TICKET_TYPE_ADULT_ID)).thenReturn(Optional.of(adultTicketType));
		when(ticketTypeRepository.findById(TICKET_TYPE_CHILD_ID)).thenReturn(Optional.of(childTicketType));
		when(priceCalculator.calculateSeatPrice(testSession, testSeat1, adultTicketType)).thenReturn(SEAT_1_PRICE);
		when(priceCalculator.calculateSeatPrice(testSession, testSeat2, childTicketType)).thenReturn(SEAT_2_PRICE);

		BookingPriceCalculator.BookingPriceResult priceResult = new BookingPriceCalculator.BookingPriceResult(
				TOTAL_PRICE, BONUS_POINTS_USED, DISCOUNT_AMOUNT, FINAL_PRICE);
		when(bookingPriceCalculator.calculateFinalPrice(eq(TOTAL_PRICE), eq(BONUS_POINTS_USED), eq(USER_ID)))
				.thenReturn(priceResult);

		savedBooking = Booking.builder().id(BOOKING_ID).user(testUser).session(testSession)
				.status(BookingStatus.PENDING).totalPrice(TOTAL_PRICE).bonusPointsUsed(BONUS_POINTS_USED)
				.bonusDiscountAmount(DISCOUNT_AMOUNT).finalPrice(FINAL_PRICE)
				.expiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES)).build();

		when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
		when(seatReservationRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
		when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(bookingResponse);

		BookingResponse result = bookingCreationService.createBooking(createRequest, testUser);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(BOOKING_ID);
		assertThat(result.bookingNumber()).isEqualTo(BOOKING_NUMBER);

		verify(bookingValidator).validateSessionForBooking(testSession);
		verify(bookingRepository).save(bookingCaptor.capture());
		verify(seatReservationRepository).saveAll(seatReservationsCaptor.capture());
		verify(bonusService).spendPoints(eq(USER_ID), eq(BONUS_POINTS_USED), any(Booking.class));

		Booking capturedBooking = bookingCaptor.getValue();
		assertThat(capturedBooking.getUser()).isEqualTo(testUser);
		assertThat(capturedBooking.getSession()).isEqualTo(testSession);
		assertThat(capturedBooking.getTotalPrice()).isEqualTo(TOTAL_PRICE);
		assertThat(capturedBooking.getBonusPointsUsed()).isEqualTo(BONUS_POINTS_USED);
		assertThat(capturedBooking.getBonusDiscountAmount()).isEqualTo(DISCOUNT_AMOUNT);
		assertThat(capturedBooking.getFinalPrice()).isEqualTo(FINAL_PRICE);
		assertThat(capturedBooking.getStatus()).isEqualTo(BookingStatus.PENDING);
		assertThat(capturedBooking.getExpiresAt()).isNotNull();

		List<SeatReservation> capturedReservations = seatReservationsCaptor.getValue();
		assertThat(capturedReservations).hasSize(2);
		assertThat(capturedReservations).allMatch(sr -> sr.getBooking() == capturedBooking);
		assertThat(capturedReservations).allMatch(sr -> sr.getStatus() == ReservationStatus.CONFIRMED);
		assertThat(capturedReservations).allMatch(sr -> sr.getReservedUntil().equals(capturedBooking.getExpiresAt()));

		SeatReservation firstReservation = capturedReservations.get(0);
		assertThat(firstReservation.getSeat()).isEqualTo(testSeat1);
		assertThat(firstReservation.getTicketType()).isEqualTo(adultTicketType);
		assertThat(firstReservation.getSeatPrice()).isEqualTo(SEAT_1_PRICE);
	}

	@Test
	void createBooking_WhenNoExistingReservation_CreatesNewOne() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID_1,
				ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.empty());
		when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID_2,
				ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.empty());
		when(seatRepository.findById(SEAT_ID_1)).thenReturn(Optional.of(testSeat1));
		when(seatRepository.findById(SEAT_ID_2)).thenReturn(Optional.of(testSeat2));
		when(seatReservationRepository.save(any(SeatReservation.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		when(ticketTypeRepository.findById(TICKET_TYPE_ADULT_ID)).thenReturn(Optional.of(adultTicketType));
		when(ticketTypeRepository.findById(TICKET_TYPE_CHILD_ID)).thenReturn(Optional.of(childTicketType));
		when(priceCalculator.calculateSeatPrice(testSession, testSeat1, adultTicketType)).thenReturn(SEAT_1_PRICE);
		when(priceCalculator.calculateSeatPrice(testSession, testSeat2, childTicketType)).thenReturn(SEAT_2_PRICE);

		BookingPriceCalculator.BookingPriceResult priceResult = new BookingPriceCalculator.BookingPriceResult(
				TOTAL_PRICE, BONUS_POINTS_USED, DISCOUNT_AMOUNT, FINAL_PRICE);
		when(bookingPriceCalculator.calculateFinalPrice(eq(TOTAL_PRICE), eq(BONUS_POINTS_USED), eq(USER_ID)))
				.thenReturn(priceResult);

		savedBooking = Booking.builder().id(BOOKING_ID).user(testUser).session(testSession)
				.status(BookingStatus.PENDING).totalPrice(TOTAL_PRICE).bonusPointsUsed(BONUS_POINTS_USED)
				.bonusDiscountAmount(DISCOUNT_AMOUNT).finalPrice(FINAL_PRICE)
				.expiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES)).build();

		when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
		when(seatReservationRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
		when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(bookingResponse);

		BookingResponse result = bookingCreationService.createBooking(createRequest, testUser);

		assertThat(result).isNotNull();
		verify(availabilityValidator).validateSeat(SESSION_ID, SEAT_ID_1);
		verify(availabilityValidator).validateSeat(SESSION_ID, SEAT_ID_2);
		verify(seatReservationRepository, never()).delete(any());
	}

	@Test
	void createBooking_WhenExistingReservationExpired_UpdatesAndReuses() {
		SeatReservation expiredReservation = SeatReservation.builder().id(1L).seat(testSeat1).session(testSession)
				.status(ReservationStatus.PENDING).reservedUntil(LocalDateTime.now().minusMinutes(1))
				.reservedByUser(testUser).build();

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID_1,
				ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.of(expiredReservation));
		when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID_2,
				ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.empty());
		when(seatRepository.findById(SEAT_ID_2)).thenReturn(Optional.of(testSeat2));
		when(seatReservationRepository.save(any(SeatReservation.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));
		when(ticketTypeRepository.findById(TICKET_TYPE_ADULT_ID)).thenReturn(Optional.of(adultTicketType));
		when(ticketTypeRepository.findById(TICKET_TYPE_CHILD_ID)).thenReturn(Optional.of(childTicketType));
		when(priceCalculator.calculateSeatPrice(testSession, testSeat1, adultTicketType)).thenReturn(SEAT_1_PRICE);
		when(priceCalculator.calculateSeatPrice(testSession, testSeat2, childTicketType)).thenReturn(SEAT_2_PRICE);

		BookingPriceCalculator.BookingPriceResult priceResult = new BookingPriceCalculator.BookingPriceResult(
				TOTAL_PRICE, BONUS_POINTS_USED, DISCOUNT_AMOUNT, FINAL_PRICE);
		when(bookingPriceCalculator.calculateFinalPrice(eq(TOTAL_PRICE), eq(BONUS_POINTS_USED), eq(USER_ID)))
				.thenReturn(priceResult);

		savedBooking = Booking.builder().id(BOOKING_ID).user(testUser).session(testSession)
				.status(BookingStatus.PENDING).totalPrice(TOTAL_PRICE).bonusPointsUsed(BONUS_POINTS_USED)
				.bonusDiscountAmount(DISCOUNT_AMOUNT).finalPrice(FINAL_PRICE)
				.expiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES)).build();

		when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
		when(seatReservationRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
		when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(bookingResponse);

		BookingResponse result = bookingCreationService.createBooking(createRequest, testUser);

		assertThat(result).isNotNull();
		verify(seatReservationRepository, never()).delete(any());
		verify(seatReservationRepository).save(expiredReservation);
		assertThat(expiredReservation.getReservedUntil()).isAfter(LocalDateTime.now());
	}

	@Test
	void createBooking_WhenSeatNotFound_ShouldThrowException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID_1,
				ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.empty());
		when(seatRepository.findById(SEAT_ID_1)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookingCreationService.createBooking(createRequest, testUser))
				.isInstanceOf(SeatNotAvailableException.class).hasMessageContaining("Seat not found");

		verify(bookingValidator).validateSessionForBooking(testSession);
		verify(bookingRepository, never()).save(any());
	}

	@Test
	void createBooking_WhenSessionNotFound_ShouldThrowException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookingCreationService.createBooking(createRequest, testUser))
				.isInstanceOf(SessionNotFoundException.class).hasMessageContaining(String.valueOf(SESSION_ID));

		verify(bookingValidator, never()).validateSessionForBooking(any());
		verify(bookingRepository, never()).save(any());
		verify(seatReservationRepository, never()).saveAll(anyList());
	}

	@Test
	void createBooking_WhenTicketTypeNotFound_ShouldThrowException() {
		SeatReservation pendingReservation = SeatReservation.builder().id(1L).seat(testSeat1).session(testSession)
				.status(ReservationStatus.PENDING).reservedUntil(LocalDateTime.now().plusMinutes(TEMP_HOLD_MINUTES))
				.reservedByUser(testUser).build();

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID_1,
				ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.of(pendingReservation));
		when(ticketTypeRepository.findById(TICKET_TYPE_ADULT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookingCreationService.createBooking(createRequest, testUser))
				.isInstanceOf(TicketTypeNotFoundException.class)
				.hasMessageContaining(String.valueOf(TICKET_TYPE_ADULT_ID));

		verify(bookingValidator).validateSessionForBooking(testSession);
		verify(bookingRepository, never()).save(any());
	}

	@Test
	void createBooking_WithoutBonusPoints() {
		BookingCreateRequest requestWithoutBonus = new BookingCreateRequest(SESSION_ID,
				Arrays.asList(new BookingCreateRequest.SeatSelectionRequest(SEAT_ID_1, TICKET_TYPE_ADULT_ID)), null);

		SeatReservation pendingReservation = SeatReservation.builder().id(1L).seat(testSeat1).session(testSession)
				.status(ReservationStatus.PENDING).reservedUntil(LocalDateTime.now().plusMinutes(TEMP_HOLD_MINUTES))
				.reservedByUser(testUser).build();

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID_1,
				ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.of(pendingReservation));
		when(ticketTypeRepository.findById(TICKET_TYPE_ADULT_ID)).thenReturn(Optional.of(adultTicketType));
		when(priceCalculator.calculateSeatPrice(testSession, testSeat1, adultTicketType)).thenReturn(SEAT_1_PRICE);

		BookingPriceCalculator.BookingPriceResult priceResult = new BookingPriceCalculator.BookingPriceResult(
				SEAT_1_PRICE, 0, BigDecimal.ZERO, SEAT_1_PRICE);
		when(bookingPriceCalculator.calculateFinalPrice(eq(SEAT_1_PRICE), eq(null), eq(USER_ID)))
				.thenReturn(priceResult);

		savedBooking = Booking.builder().id(BOOKING_ID).user(testUser).session(testSession)
				.status(BookingStatus.PENDING).totalPrice(SEAT_1_PRICE).bonusPointsUsed(0)
				.bonusDiscountAmount(BigDecimal.ZERO).finalPrice(SEAT_1_PRICE)
				.expiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES)).build();

		when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
		when(seatReservationRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
		when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(bookingResponse);

		BookingResponse result = bookingCreationService.createBooking(requestWithoutBonus, testUser);

		assertThat(result).isNotNull();

		verify(bookingRepository).save(bookingCaptor.capture());
		Booking capturedBooking = bookingCaptor.getValue();
		assertThat(capturedBooking.getBonusPointsUsed()).isEqualTo(0);
		assertThat(capturedBooking.getBonusDiscountAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(capturedBooking.getFinalPrice()).isEqualTo(SEAT_1_PRICE);

		verify(bonusService, never()).spendPoints(anyLong(), any(Integer.class), any(Booking.class));
	}

	@Test
	void createBooking_ShouldSetCorrectExpirationTime() {
		LocalDateTime now = LocalDateTime.now();

		SeatReservation pendingReservation1 = SeatReservation.builder().id(1L).seat(testSeat1).session(testSession)
				.status(ReservationStatus.PENDING).reservedUntil(LocalDateTime.now().plusMinutes(TEMP_HOLD_MINUTES))
				.reservedByUser(testUser).build();

		SeatReservation pendingReservation2 = SeatReservation.builder().id(2L).seat(testSeat2).session(testSession)
				.status(ReservationStatus.PENDING).reservedUntil(LocalDateTime.now().plusMinutes(TEMP_HOLD_MINUTES))
				.reservedByUser(testUser).build();

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID_1,
				ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.of(pendingReservation1));
		when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID_2,
				ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.of(pendingReservation2));
		when(ticketTypeRepository.findById(TICKET_TYPE_ADULT_ID)).thenReturn(Optional.of(adultTicketType));
		when(ticketTypeRepository.findById(TICKET_TYPE_CHILD_ID)).thenReturn(Optional.of(childTicketType));
		when(priceCalculator.calculateSeatPrice(testSession, testSeat1, adultTicketType)).thenReturn(SEAT_1_PRICE);
		when(priceCalculator.calculateSeatPrice(testSession, testSeat2, childTicketType)).thenReturn(SEAT_2_PRICE);

		BookingPriceCalculator.BookingPriceResult priceResult = new BookingPriceCalculator.BookingPriceResult(
				TOTAL_PRICE, BONUS_POINTS_USED, DISCOUNT_AMOUNT, FINAL_PRICE);
		when(bookingPriceCalculator.calculateFinalPrice(eq(TOTAL_PRICE), eq(BONUS_POINTS_USED), eq(USER_ID)))
				.thenReturn(priceResult);

		savedBooking = Booking.builder().id(BOOKING_ID).user(testUser).session(testSession)
				.status(BookingStatus.PENDING).totalPrice(TOTAL_PRICE).bonusPointsUsed(BONUS_POINTS_USED)
				.bonusDiscountAmount(DISCOUNT_AMOUNT).finalPrice(FINAL_PRICE)
				.expiresAt(now.plusMinutes(EXPIRATION_MINUTES)).build();

		when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
		when(seatReservationRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
		when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(bookingResponse);

		bookingCreationService.createBooking(createRequest, testUser);

		verify(bookingRepository).save(bookingCaptor.capture());
		Booking capturedBooking = bookingCaptor.getValue();

		assertThat(capturedBooking.getExpiresAt()).isNotNull();
		assertThat(capturedBooking.getExpiresAt()).isAfter(now);
		assertThat(capturedBooking.getExpiresAt()).isBefore(now.plusMinutes(EXPIRATION_MINUTES + 1));
	}
}