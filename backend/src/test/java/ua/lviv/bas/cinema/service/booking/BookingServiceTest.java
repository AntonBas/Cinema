package ua.lviv.bas.cinema.service.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import ua.lviv.bas.cinema.domain.BookedSeat;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.BookedSeatStatus;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.booking.request.BookingCreateRequest;
import ua.lviv.bas.cinema.dto.booking.request.BookingCreateRequest.SeatSelectionRequest;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingOperationException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingValidationException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeNotFoundException;
import ua.lviv.bas.cinema.mapper.BookingMapper;
import ua.lviv.bas.cinema.repository.BookedSeatRepository;
import ua.lviv.bas.cinema.repository.BookingRepository;
import ua.lviv.bas.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.repository.TicketTypeRepository;
import ua.lviv.bas.cinema.service.user.BonusService;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

	@Mock
	private BookingRepository bookingRepository;

	@Mock
	private SessionRepository sessionRepository;

	@Mock
	private SeatRepository seatRepository;

	@Mock
	private TicketTypeRepository ticketTypeRepository;

	@Mock
	private BookedSeatRepository bookedSeatRepository;

	@Mock
	private BookingMapper bookingMapper;

	@Mock
	private SeatAvailabilityService seatAvailabilityService;

	@Mock
	private BonusService bonusService;

	@InjectMocks
	private BookingService bookingService;

	private User testUser;
	private Session testSession;
	private Seat testSeat1;
	private Seat testSeat2;
	private TicketType adultTicketType;
	private TicketType childTicketType;
	private BookingCreateRequest createRequest;
	private Booking testBooking;
	private BookedSeat bookedSeat1;
	private BookedSeat bookedSeat2;
	private BookingResponse bookingResponse;
	private LocalDateTime createdAt;

	private final Long USER_ID = 1L;
	private final Long SESSION_ID = 2L;
	private final Long SEAT_ID_1 = 3L;
	private final Long SEAT_ID_2 = 4L;
	private final Long TICKET_TYPE_ADULT_ID = 5L;
	private final Long TICKET_TYPE_CHILD_ID = 6L;
	private final Long BOOKING_ID = 7L;
	private final BigDecimal BASE_PRICE = new BigDecimal("200.00");

	@BeforeEach
	void setUp() {
		createdAt = LocalDateTime.now();

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
		adultTicketType.setPriceMultiplier(new BigDecimal("1.0"));

		childTicketType = new TicketType();
		childTicketType.setId(TICKET_TYPE_CHILD_ID);
		childTicketType.setDisplayName("Child");
		childTicketType.setPriceMultiplier(new BigDecimal("1.4"));

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

		bookedSeat1 = BookedSeat.builder().id(1L).seat(testSeat1).session(testSession).ticketType(adultTicketType)
				.seatPrice(new BigDecimal("200.00")).status(BookedSeatStatus.PENDING).build();

		bookedSeat2 = BookedSeat.builder().id(2L).seat(testSeat2).session(testSession).ticketType(childTicketType)
				.seatPrice(new BigDecimal("280.00")).status(BookedSeatStatus.PENDING).build();

		testBooking = Booking.builder().id(BOOKING_ID).user(testUser).session(testSession).status(BookingStatus.PENDING)
				.totalPrice(new BigDecimal("480.00")).bonusPointsUsed(100).bonusDiscountAmount(new BigDecimal("100.00"))
				.finalPrice(new BigDecimal("380.00")).expiresAt(LocalDateTime.now().plusMinutes(20))
				.createdAt(createdAt).bookedSeats(Arrays.asList(bookedSeat1, bookedSeat2)).build();

		bookedSeat1.setBooking(testBooking);
		bookedSeat2.setBooking(testBooking);

		bookingResponse = new BookingResponse();
		bookingResponse.setId(BOOKING_ID);
		bookingResponse.setTotalPrice(new BigDecimal("480.00"));
		bookingResponse.setFinalPrice(new BigDecimal("380.00"));
		bookingResponse.setBookingNumber("BK-2024-00007");
	}

	@Test
	void createBooking_Success() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findById(SEAT_ID_1)).thenReturn(Optional.of(testSeat1));
		when(seatRepository.findById(SEAT_ID_2)).thenReturn(Optional.of(testSeat2));
		when(ticketTypeRepository.findById(TICKET_TYPE_ADULT_ID)).thenReturn(Optional.of(adultTicketType));
		when(ticketTypeRepository.findById(TICKET_TYPE_CHILD_ID)).thenReturn(Optional.of(childTicketType));
		when(seatAvailabilityService.calculateSeatPrice(testSession, testSeat1, adultTicketType))
				.thenReturn(new BigDecimal("200.00"));
		when(seatAvailabilityService.calculateSeatPrice(testSession, testSeat2, childTicketType))
				.thenReturn(new BigDecimal("280.00"));
		when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
		when(bookedSeatRepository.saveAll(anyList())).thenReturn(Arrays.asList(bookedSeat1, bookedSeat2));
		when(bookingMapper.toBookingResponse(testBooking)).thenReturn(bookingResponse);

		BookingResponse result = bookingService.createBooking(createRequest, testUser);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(BOOKING_ID);
		assertThat(result.getTotalPrice()).isEqualByComparingTo("480.00");
		assertThat(result.getFinalPrice()).isEqualByComparingTo("380.00");

		verify(bonusService).validateBonusPointsForBooking(eq(USER_ID), eq(100), any(BigDecimal.class));
		verify(seatAvailabilityService, times(2)).validateSeatAvailability(eq(SESSION_ID), anyLong());
		verify(bookingRepository).save(any(Booking.class));
		verify(bookedSeatRepository).saveAll(anyList());
		verify(bonusService).spendBonusPointsForBooking(eq(USER_ID), eq(100), any(Booking.class), any(String.class));
	}

	@Test
	void createBooking_WhenSessionNotFound_ShouldThrowException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookingService.createBooking(createRequest, testUser))
				.isInstanceOf(SessionNotFoundException.class);

		verify(sessionRepository).findById(SESSION_ID);
		verify(bookingRepository, never()).save(any());
	}

	@Test
	void createBooking_WhenSessionNotScheduled_ShouldThrowException() {
		testSession.setStatus(CinemaSessionStatus.CANCELLED);
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));

		assertThatThrownBy(() -> bookingService.createBooking(createRequest, testUser))
				.isInstanceOf(BookingValidationException.class).hasMessageContaining("not available");

		verify(sessionRepository).findById(SESSION_ID);
	}

	@Test
	void createBooking_WhenSessionAlreadyStarted_ShouldThrowException() {
		testSession.setStartTime(LocalDateTime.now().minusHours(1));
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));

		assertThatThrownBy(() -> bookingService.createBooking(createRequest, testUser))
				.isInstanceOf(BookingValidationException.class).hasMessageContaining("already started");

		verify(sessionRepository).findById(SESSION_ID);
	}

	@Test
	void createBooking_WhenSessionTooClose_ShouldThrowException() {
		testSession.setStartTime(LocalDateTime.now().plusMinutes(20));
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));

		assertThatThrownBy(() -> bookingService.createBooking(createRequest, testUser))
				.isInstanceOf(BookingValidationException.class).hasMessageContaining("starts in less than 30 minutes");

		verify(sessionRepository).findById(SESSION_ID);
	}

	@Test
	void createBooking_WhenSeatNotFound_ShouldThrowException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findById(SEAT_ID_1)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookingService.createBooking(createRequest, testUser))
				.isInstanceOf(ua.lviv.bas.cinema.exception.domain.cinema.SeatNotFoundException.class);

		verify(seatRepository).findById(SEAT_ID_1);
	}

	@Test
	void createBooking_WhenTicketTypeNotFound_ShouldThrowException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findById(SEAT_ID_1)).thenReturn(Optional.of(testSeat1));
		when(ticketTypeRepository.findById(TICKET_TYPE_ADULT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookingService.createBooking(createRequest, testUser))
				.isInstanceOf(TicketTypeNotFoundException.class);

		verify(ticketTypeRepository).findById(TICKET_TYPE_ADULT_ID);
	}

	@Test
	void createBooking_WhenSeatNotAvailable_ShouldThrowException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findById(SEAT_ID_1)).thenReturn(Optional.of(testSeat1));
		when(ticketTypeRepository.findById(TICKET_TYPE_ADULT_ID)).thenReturn(Optional.of(adultTicketType));

		doThrow(ua.lviv.bas.cinema.exception.domain.booking.SeatNotAvailableException.class)
				.when(seatAvailabilityService).validateSeatAvailability(SESSION_ID, SEAT_ID_1);

		assertThatThrownBy(() -> bookingService.createBooking(createRequest, testUser))
				.isInstanceOf(ua.lviv.bas.cinema.exception.domain.booking.SeatNotAvailableException.class);

		verify(seatAvailabilityService).validateSeatAvailability(SESSION_ID, SEAT_ID_1);
	}

	@Test
	void createBooking_WhenInvalidBonusPoints_ShouldThrowException() {
		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findById(SEAT_ID_1)).thenReturn(Optional.of(testSeat1));
		when(seatRepository.findById(SEAT_ID_2)).thenReturn(Optional.of(testSeat2));
		when(ticketTypeRepository.findById(TICKET_TYPE_ADULT_ID)).thenReturn(Optional.of(adultTicketType));
		when(ticketTypeRepository.findById(TICKET_TYPE_CHILD_ID)).thenReturn(Optional.of(childTicketType));
		when(seatAvailabilityService.calculateSeatPrice(testSession, testSeat1, adultTicketType))
				.thenReturn(new BigDecimal("200.00"));
		when(seatAvailabilityService.calculateSeatPrice(testSession, testSeat2, childTicketType))
				.thenReturn(new BigDecimal("280.00"));

		doThrow(IllegalArgumentException.class).when(bonusService).validateBonusPointsForBooking(eq(USER_ID), eq(100),
				any(BigDecimal.class));

		assertThatThrownBy(() -> bookingService.createBooking(createRequest, testUser))
				.isInstanceOf(IllegalArgumentException.class);

		verify(bonusService).validateBonusPointsForBooking(eq(USER_ID), eq(100), any(BigDecimal.class));
	}

	@Test
	void createBooking_WithoutBonusPoints() {
		createRequest.setBonusPointsToUse(0);
		List<SeatSelectionRequest> seats = Collections.singletonList(createRequest.getSeats().get(0));
		createRequest.setSeats(seats);

		Booking bookingWithoutBonus = Booking.builder().id(BOOKING_ID).user(testUser).session(testSession)
				.status(BookingStatus.PENDING).totalPrice(new BigDecimal("200.00")).bonusPointsUsed(0)
				.bonusDiscountAmount(BigDecimal.ZERO).finalPrice(new BigDecimal("200.00"))
				.expiresAt(LocalDateTime.now().plusMinutes(20)).createdAt(createdAt)
				.bookedSeats(Arrays.asList(bookedSeat1)).build();

		when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
		when(seatRepository.findById(SEAT_ID_1)).thenReturn(Optional.of(testSeat1));
		when(ticketTypeRepository.findById(TICKET_TYPE_ADULT_ID)).thenReturn(Optional.of(adultTicketType));
		when(seatAvailabilityService.calculateSeatPrice(testSession, testSeat1, adultTicketType))
				.thenReturn(new BigDecimal("200.00"));
		when(bookingRepository.save(any(Booking.class))).thenReturn(bookingWithoutBonus);
		when(bookingMapper.toBookingResponse(bookingWithoutBonus)).thenReturn(bookingResponse);

		BookingResponse result = bookingService.createBooking(createRequest, testUser);

		assertThat(result).isNotNull();
		verify(bonusService, never()).validateBonusPointsForBooking(anyLong(), any(Integer.class),
				any(BigDecimal.class));
		verify(bonusService, never()).spendBonusPointsForBooking(anyLong(), any(Integer.class), any(Booking.class),
				any(String.class));
	}

	@Test
	void getBookingById_Success() {
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));
		when(bookingMapper.toBookingResponse(testBooking)).thenReturn(bookingResponse);

		BookingResponse result = bookingService.getBookingById(BOOKING_ID, testUser);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(BOOKING_ID);
		verify(bookingRepository).findByIdAndUserId(BOOKING_ID, USER_ID);
		verify(bookingMapper).toBookingResponse(testBooking);
	}

	@Test
	void getBookingById_WhenBookingNotFound_ShouldThrowException() {
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookingService.getBookingById(BOOKING_ID, testUser))
				.isInstanceOf(BookingNotFoundException.class);

		verify(bookingRepository).findByIdAndUserId(BOOKING_ID, USER_ID);
	}

	@Test
	void getUserBookings_WithStatusFilter() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Booking> bookingPage = new PageImpl<>(Arrays.asList(testBooking), pageable, 1);

		when(bookingRepository.findByUserIdAndStatusOrderByCreatedAtDesc(eq(USER_ID), eq(BookingStatus.PENDING),
				eq(pageable))).thenReturn(bookingPage);
		when(bookingMapper.toBookingResponse(testBooking)).thenReturn(bookingResponse);

		Page<BookingResponse> result = bookingService.getUserBookings(USER_ID, BookingStatus.PENDING, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getId()).isEqualTo(BOOKING_ID);
		verify(bookingRepository).findByUserIdAndStatusOrderByCreatedAtDesc(eq(USER_ID), eq(BookingStatus.PENDING),
				eq(pageable));
	}

	@Test
	void getUserBookings_WithoutStatusFilter() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Booking> bookingPage = new PageImpl<>(Arrays.asList(testBooking), pageable, 1);

		when(bookingRepository.findByUserIdOrderByCreatedAtDesc(eq(USER_ID), eq(pageable))).thenReturn(bookingPage);
		when(bookingMapper.toBookingResponse(testBooking)).thenReturn(bookingResponse);

		Page<BookingResponse> result = bookingService.getUserBookings(USER_ID, null, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getId()).isEqualTo(BOOKING_ID);
		verify(bookingRepository).findByUserIdOrderByCreatedAtDesc(eq(USER_ID), eq(pageable));
	}

	@Test
	void cancelBooking_Success_Pending() {
		testBooking.setStatus(BookingStatus.PENDING);
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));
		when(bookingRepository.save(testBooking)).thenReturn(testBooking);

		bookingService.cancelBooking(BOOKING_ID, testUser);

		assertThat(testBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
		testBooking.getBookedSeats().forEach(bs -> assertThat(bs.getStatus()).isEqualTo(BookedSeatStatus.CANCELLED));
		verify(bookingRepository).save(testBooking);
		verify(bonusService).refundBonusPointsForCancellation(testBooking);
	}

	@Test
	void cancelBooking_Success_Confirmed() {
		testBooking.setStatus(BookingStatus.CONFIRMED);
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));
		when(bookingRepository.save(testBooking)).thenReturn(testBooking);

		bookingService.cancelBooking(BOOKING_ID, testUser);

		assertThat(testBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
		verify(bookingRepository).save(testBooking);
		verify(bonusService).refundBonusPointsForCancellation(testBooking);
	}

	@Test
	void cancelBooking_WhenBookingNotFound_ShouldThrowException() {
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookingService.cancelBooking(BOOKING_ID, testUser))
				.isInstanceOf(BookingNotFoundException.class);

		verify(bookingRepository).findByIdAndUserId(BOOKING_ID, USER_ID);
		verify(bookingRepository, never()).save(any());
	}

	@Test
	void cancelBooking_WhenCannotBeCancelled_ShouldThrowException() {
		testBooking.setStatus(BookingStatus.EXPIRED);
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));

		assertThatThrownBy(() -> bookingService.cancelBooking(BOOKING_ID, testUser))
				.isInstanceOf(BookingValidationException.class).hasMessageContaining("cannot be cancelled");

		verify(bookingRepository, never()).save(any());
		verify(bonusService, never()).refundBonusPointsForCancellation(any());
	}

	@Test
	void confirmBooking_Success() {
		testBooking.setStatus(BookingStatus.PENDING);
		when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(testBooking));
		when(bookingRepository.save(testBooking)).thenReturn(testBooking);

		bookingService.confirmBooking(BOOKING_ID);

		assertThat(testBooking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
		testBooking.getBookedSeats().forEach(bs -> assertThat(bs.getStatus()).isEqualTo(BookedSeatStatus.CONFIRMED));
		verify(bookingRepository).save(testBooking);
	}

	@Test
	void confirmBooking_WhenBookingNotFound_ShouldThrowException() {
		when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookingService.confirmBooking(BOOKING_ID))
				.isInstanceOf(BookingNotFoundException.class);

		verify(bookingRepository).findById(BOOKING_ID);
		verify(bookingRepository, never()).save(any());
	}

	@Test
	void confirmBooking_WhenNotPending_ShouldThrowException() {
		testBooking.setStatus(BookingStatus.CONFIRMED);
		when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(testBooking));

		assertThatThrownBy(() -> bookingService.confirmBooking(BOOKING_ID))
				.isInstanceOf(BookingOperationException.class).hasMessage("Only pending bookings can be confirmed");

		verify(bookingRepository).findById(BOOKING_ID);
		verify(bookingRepository, never()).save(any());
	}

	@Test
	void expireBooking_Success() {
		testBooking.setStatus(BookingStatus.PENDING);
		when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(testBooking));
		when(bookingRepository.save(testBooking)).thenReturn(testBooking);

		bookingService.expireBooking(BOOKING_ID);

		assertThat(testBooking.getStatus()).isEqualTo(BookingStatus.EXPIRED);
		testBooking.getBookedSeats().forEach(bs -> assertThat(bs.getStatus()).isEqualTo(BookedSeatStatus.EXPIRED));
		verify(bookingRepository).save(testBooking);
		verify(bonusService).refundBonusPointsForCancellation(testBooking);
	}

	@Test
	void expireBooking_WhenBookingNotFound_ShouldThrowException() {
		when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookingService.expireBooking(BOOKING_ID)).isInstanceOf(BookingNotFoundException.class);

		verify(bookingRepository).findById(BOOKING_ID);
		verify(bookingRepository, never()).save(any());
	}

	@Test
	void expireBooking_WhenNotPending_ShouldThrowException() {
		testBooking.setStatus(BookingStatus.CONFIRMED);
		when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(testBooking));

		assertThatThrownBy(() -> bookingService.expireBooking(BOOKING_ID)).isInstanceOf(BookingOperationException.class)
				.hasMessage("Cannot expire a booking that is not in PENDING status");

		verify(bookingRepository).findById(BOOKING_ID);
		verify(bookingRepository, never()).save(any());
		verify(bonusService, never()).refundBonusPointsForCancellation(any());
	}

	@Test
	void getAvailableBonusPointsForBooking_Success() {
		BigDecimal totalPrice = new BigDecimal("480.00");
		when(bonusService.getAvailablePointsForRedemption(USER_ID, totalPrice)).thenReturn(100);

		Integer result = bookingService.getAvailableBonusPointsForBooking(USER_ID, totalPrice);

		assertThat(result).isEqualTo(100);
		verify(bonusService).getAvailablePointsForRedemption(USER_ID, totalPrice);
	}
}