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
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.SeatReservation;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.ReservationStatus;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.booking.request.BookingCreateRequest;
import ua.lviv.bas.cinema.dto.booking.request.BookingCreateRequest.SeatSelectionRequest;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.SeatNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeNotFoundException;
import ua.lviv.bas.cinema.mapper.BookingMapper;
import ua.lviv.bas.cinema.repository.SeatReservationRepository;
import ua.lviv.bas.cinema.repository.BookingRepository;
import ua.lviv.bas.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.repository.TicketTypeRepository;
import ua.lviv.bas.cinema.service.booking.availability.AvailabilityValidator;
import ua.lviv.bas.cinema.service.shared.NumberGeneratorService;
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
	private SeatReservationRepository bookedSeatRepository;

	@Mock
	private BookingMapper bookingMapper;

	@Mock
	private AvailabilityValidator availabilityValidator;

	@Mock
	private BonusService bonusService;

	@Mock
	private PriceCalculatorService priceCalculator;

	@Mock
	private NumberGeneratorService numberGenerator;

	@Mock
	private BookingPriceCalculator bookingPriceCalculator;

	@Mock
	private BookingValidator bookingValidator;

	@InjectMocks
	private BookingCreationService bookingCreationService;

	private User testUser;
	private Session testSession;
	private Seat testSeat1;
	private Seat testSeat2;
	private TicketType adultTicketType;
	private TicketType childTicketType;
	private BookingCreateRequest createRequest;

	private static final Long USER_ID = 1L;
	private static final Long SESSION_ID = 2L;
	private static final Long SEAT_ID_1 = 3L;
	private static final Long SEAT_ID_2 = 4L;
	private static final Long TICKET_TYPE_ADULT_ID = 5L;
	private static final Long TICKET_TYPE_CHILD_ID = 6L;
	private static final BigDecimal BASE_PRICE = new BigDecimal("200.00");

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setId(USER_ID);
		testUser.setEmail("test@example.com");

		Movie movie = new Movie();
		movie.setTitle("Test Movie");

		CinemaHall hall = new CinemaHall();
		hall.setName("Hall A");

		testSession = new Session();
		testSession.setId(SESSION_ID);
		testSession.setMovie(movie);
		testSession.setHall(hall);
		testSession.setBasePrice(BASE_PRICE);
		testSession.setStatus(CinemaSessionStatus.SCHEDULED);
		testSession.setStartTime(LocalDateTime.now().plusHours(2));

		testSeat1 = new Seat();
		testSeat1.setId(SEAT_ID_1);
		testSeat1.setRow(1);
		testSeat1.setNumber(1);
		testSeat1.setSeatType(SeatType.STANDARD);
		testSeat1.setActive(true);

		testSeat2 = new Seat();
		testSeat2.setId(SEAT_ID_2);
		testSeat2.setRow(1);
		testSeat2.setNumber(2);
		testSeat2.setSeatType(SeatType.VIP);
		testSeat2.setActive(true);

		adultTicketType = new TicketType();
		adultTicketType.setId(TICKET_TYPE_ADULT_ID);
		adultTicketType.setDisplayName("Adult");

		childTicketType = new TicketType();
		childTicketType.setId(TICKET_TYPE_CHILD_ID);
		childTicketType.setDisplayName("Child");

		SeatSelectionRequest seatSelection1 = new SeatSelectionRequest();
		seatSelection1.setSeatId(SEAT_ID_1);
		seatSelection1.setTicketTypeId(TICKET_TYPE_ADULT_ID);

		SeatSelectionRequest seatSelection2 = new SeatSelectionRequest();
		seatSelection2.setSeatId(SEAT_ID_2);
		seatSelection2.setTicketTypeId(TICKET_TYPE_CHILD_ID);

		createRequest = new BookingCreateRequest();
		createRequest.setSessionId(SESSION_ID);
		createRequest.setSeats(Arrays.asList(seatSelection1, seatSelection2));
		createRequest.setBonusPointsToUse(100);
	}

	@Test
	void createBooking_Success() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findById(SEAT_ID_1)).thenReturn(Optional.of(testSeat1));
		when(seatRepository.findById(SEAT_ID_2)).thenReturn(Optional.of(testSeat2));
		when(ticketTypeRepository.findById(TICKET_TYPE_ADULT_ID)).thenReturn(Optional.of(adultTicketType));
		when(ticketTypeRepository.findById(TICKET_TYPE_CHILD_ID)).thenReturn(Optional.of(childTicketType));
		when(priceCalculator.calculateSeatPrice(testSession, testSeat1, adultTicketType))
				.thenReturn(new BigDecimal("200.00"));
		when(priceCalculator.calculateSeatPrice(testSession, testSeat2, childTicketType))
				.thenReturn(new BigDecimal("280.00"));

		BookingPriceCalculator.BookingPriceResult priceResult = new BookingPriceCalculator.BookingPriceResult(
				new BigDecimal("480.00"), 100, new BigDecimal("100.00"), new BigDecimal("380.00"));
		when(bookingPriceCalculator.calculateFinalPrice(any(BigDecimal.class), eq(100), eq(USER_ID)))
				.thenReturn(priceResult);

		Booking booking = Booking.builder().id(1L).user(testUser).session(testSession).status(BookingStatus.PENDING)
				.totalPrice(new BigDecimal("480.00")).bonusPointsUsed(100).bonusDiscountAmount(new BigDecimal("100.00"))
				.finalPrice(new BigDecimal("380.00")).expiresAt(LocalDateTime.now().plusMinutes(20))
				.bookedSeats(Arrays.asList(
						SeatReservation.builder().seat(testSeat1).session(testSession).ticketType(adultTicketType)
								.seatPrice(new BigDecimal("200.00")).status(ReservationStatus.PENDING).build(),
						SeatReservation.builder().seat(testSeat2).session(testSession).ticketType(childTicketType)
								.seatPrice(new BigDecimal("280.00")).status(ReservationStatus.PENDING).build()))
				.build();

		when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

		BookingResponse bookingResponse = new BookingResponse();
		bookingResponse.setId(1L);
		when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(bookingResponse);
		when(numberGenerator.generateBookingNumber(any(Booking.class))).thenReturn("BK-2024-00001");

		BookingResponse result = bookingCreationService.createBooking(createRequest, testUser);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);

		verify(bookingValidator).validateSessionForBooking(testSession);
		verify(availabilityValidator).validateSeat(SESSION_ID, SEAT_ID_1);
		verify(availabilityValidator).validateSeat(SESSION_ID, SEAT_ID_2);
		verify(bookingRepository).save(any(Booking.class));
		verify(bookedSeatRepository).saveAll(anyList());
		verify(bonusService).spendBonusPointsForBooking(eq(USER_ID), eq(100), any(Booking.class), any(String.class));
	}

	@Test
	void createBooking_WhenSessionNotFound_ShouldThrowException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookingCreationService.createBooking(createRequest, testUser))
				.isInstanceOf(SessionNotFoundException.class);
	}

	@Test
	void createBooking_WhenSeatNotFound_ShouldThrowException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findById(SEAT_ID_1)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookingCreationService.createBooking(createRequest, testUser))
				.isInstanceOf(SeatNotFoundException.class);
	}

	@Test
	void createBooking_WhenTicketTypeNotFound_ShouldThrowException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findById(SEAT_ID_1)).thenReturn(Optional.of(testSeat1));
		when(ticketTypeRepository.findById(TICKET_TYPE_ADULT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookingCreationService.createBooking(createRequest, testUser))
				.isInstanceOf(TicketTypeNotFoundException.class);
	}

	@Test
	void createBooking_WithoutBonusPoints() {
		createRequest.setBonusPointsToUse(null);
		List<SeatSelectionRequest> singleSeat = Arrays.asList(createRequest.getSeats().get(0));
		createRequest.setSeats(singleSeat);

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findById(SEAT_ID_1)).thenReturn(Optional.of(testSeat1));
		when(ticketTypeRepository.findById(TICKET_TYPE_ADULT_ID)).thenReturn(Optional.of(adultTicketType));
		when(priceCalculator.calculateSeatPrice(testSession, testSeat1, adultTicketType))
				.thenReturn(new BigDecimal("200.00"));

		BookingPriceCalculator.BookingPriceResult priceResult = new BookingPriceCalculator.BookingPriceResult(
				new BigDecimal("200.00"), 0, BigDecimal.ZERO, new BigDecimal("200.00"));
		when(bookingPriceCalculator.calculateFinalPrice(any(BigDecimal.class), eq(null), eq(USER_ID)))
				.thenReturn(priceResult);

		Booking booking = Booking.builder().id(1L).user(testUser).session(testSession).status(BookingStatus.PENDING)
				.totalPrice(new BigDecimal("200.00")).bonusPointsUsed(0).bonusDiscountAmount(BigDecimal.ZERO)
				.finalPrice(new BigDecimal("200.00")).build();

		when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
		when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(new BookingResponse());

		BookingResponse result = bookingCreationService.createBooking(createRequest, testUser);

		assertThat(result).isNotNull();
		verify(bonusService, never()).spendBonusPointsForBooking(anyLong(), any(), any(Booking.class),
				any(String.class));
	}
}