package ua.lviv.bas.cinema.service.booking.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
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
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingOperationException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingValidationException;
import ua.lviv.bas.cinema.mapper.BookingMapper;
import ua.lviv.bas.cinema.repository.BookingRepository;
import ua.lviv.bas.cinema.service.booking.creation.BookingValidator;
import ua.lviv.bas.cinema.service.shared.NumberGeneratorService;
import ua.lviv.bas.cinema.service.user.BonusService;

@ExtendWith(MockitoExtension.class)
public class BookingManagementServiceTest {

	@Mock
	private BookingRepository bookingRepository;

	@Mock
	private BookingMapper bookingMapper;

	@Mock
	private BookingValidator bookingValidator;

	@Mock
	private BonusService bonusService;

	@Mock
	private NumberGeneratorService numberGenerator;

	@InjectMocks
	private BookingManagementService bookingManagementService;

	private User testUser;
	private Booking testBooking;
	private SeatReservation seatReservation1;
	private SeatReservation seatReservation2;
	private BookingResponse bookingResponse;

	private static final Long USER_ID = 1L;
	private static final Long BOOKING_ID = 2L;
	private static final String BOOKING_NUMBER = "BK-2024-00001";

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setId(USER_ID);
		testUser.setEmail("test@example.com");

		Movie movie = new Movie();
		movie.setTitle("Test Movie");

		CinemaHall hall = new CinemaHall();
		hall.setName("Hall A");

		Session session = new Session();
		session.setId(1L);
		session.setMovie(movie);
		session.setHall(hall);
		session.setStatus(CinemaSessionStatus.SCHEDULED);
		session.setStartTime(LocalDateTime.now().plusHours(2));

		Seat seat1 = new Seat();
		seat1.setId(1L);
		seat1.setSeatType(SeatType.STANDARD);

		Seat seat2 = new Seat();
		seat2.setId(2L);
		seat2.setSeatType(SeatType.VIP);

		TicketType ticketType = new TicketType();
		ticketType.setId(1L);
		ticketType.setDisplayName("Adult");

		seatReservation1 = SeatReservation.builder().id(1L).seat(seat1).session(session).ticketType(ticketType)
				.seatPrice(new BigDecimal("200.00")).status(ReservationStatus.PENDING).build();

		seatReservation2 = SeatReservation.builder().id(2L).seat(seat2).session(session).ticketType(ticketType)
				.seatPrice(new BigDecimal("300.00")).status(ReservationStatus.PENDING).build();

		testBooking = Booking.builder().id(BOOKING_ID).user(testUser).session(session).status(BookingStatus.PENDING)
				.totalPrice(new BigDecimal("500.00")).bonusPointsUsed(100).bonusDiscountAmount(new BigDecimal("100.00"))
				.finalPrice(new BigDecimal("400.00")).expiresAt(LocalDateTime.now().plusMinutes(20))
				.seatReservations(Arrays.asList(seatReservation1, seatReservation2)).build();

		seatReservation1.setBooking(testBooking);
		seatReservation2.setBooking(testBooking);

		bookingResponse = new BookingResponse(BOOKING_ID, BOOKING_NUMBER, BookingStatus.PENDING, 1L,
				LocalDateTime.now(), "Test Movie", "Hall A", new BigDecimal("500.00"), 100, new BigDecimal("100.00"),
				new BigDecimal("400.00"), null, null, null, null);
	}

	@Test
	void getBookingById_Success() {
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));
		when(bookingMapper.toBookingResponse(testBooking)).thenReturn(bookingResponse);
		when(numberGenerator.generateBookingNumber(testBooking)).thenReturn(BOOKING_NUMBER);

		BookingResponse result = bookingManagementService.getBookingById(BOOKING_ID, testUser);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(BOOKING_ID);
		assertThat(result.bookingNumber()).isEqualTo(BOOKING_NUMBER);
	}

	@Test
	void getBookingById_WhenBookingNotFound_ShouldThrowException() {
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookingManagementService.getBookingById(BOOKING_ID, testUser))
				.isInstanceOf(BookingNotFoundException.class);
	}

	@Test
	void getUserBookings_WithStatusFilter() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Booking> bookingPage = new PageImpl<>(Arrays.asList(testBooking), pageable, 1);

		when(bookingRepository.findByUserIdAndStatusOrderByCreatedAtDesc(eq(USER_ID), eq(BookingStatus.PENDING),
				eq(pageable))).thenReturn(bookingPage);
		when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(bookingResponse);
		when(numberGenerator.generateBookingNumber(any(Booking.class))).thenReturn(BOOKING_NUMBER);

		Page<BookingResponse> result = bookingManagementService.getUserBookings(USER_ID, BookingStatus.PENDING,
				pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).id()).isEqualTo(BOOKING_ID);
	}

	@Test
	void getUserBookings_WithoutStatusFilter() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Booking> bookingPage = new PageImpl<>(Arrays.asList(testBooking), pageable, 1);

		when(bookingRepository.findByUserIdOrderByCreatedAtDesc(eq(USER_ID), eq(pageable))).thenReturn(bookingPage);
		when(bookingMapper.toBookingResponse(any(Booking.class))).thenReturn(bookingResponse);
		when(numberGenerator.generateBookingNumber(any(Booking.class))).thenReturn(BOOKING_NUMBER);

		Page<BookingResponse> result = bookingManagementService.getUserBookings(USER_ID, null, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).id()).isEqualTo(BOOKING_ID);
	}

	@Test
	void cancelBooking_Success_Pending() {
		testBooking.setStatus(BookingStatus.PENDING);

		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));
		when(bookingValidator.canBookingBeCancelled(testBooking)).thenReturn(true);
		when(bookingRepository.save(testBooking)).thenReturn(testBooking);

		bookingManagementService.cancelBooking(BOOKING_ID, testUser);

		assertThat(testBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
		assertThat(seatReservation1.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
		assertThat(seatReservation2.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
	}

	@Test
	void cancelBooking_WhenBookingNotFound_ShouldThrowException() {
		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookingManagementService.cancelBooking(BOOKING_ID, testUser))
				.isInstanceOf(BookingNotFoundException.class);
	}

	@Test
	void cancelBooking_WhenCannotBeCancelled_ShouldThrowException() {
		testBooking.setStatus(BookingStatus.EXPIRED);

		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));
		when(bookingValidator.canBookingBeCancelled(testBooking)).thenReturn(false);

		assertThatThrownBy(() -> bookingManagementService.cancelBooking(BOOKING_ID, testUser))
				.isInstanceOf(BookingValidationException.class);
	}

	@Test
	void confirmBooking_Success() {
		testBooking.setStatus(BookingStatus.PENDING);

		when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(testBooking));
		when(bookingRepository.save(testBooking)).thenReturn(testBooking);

		bookingManagementService.confirmBooking(BOOKING_ID);

		assertThat(testBooking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
		assertThat(seatReservation1.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
		assertThat(seatReservation2.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
	}

	@Test
	void confirmBooking_WhenBookingNotFound_ShouldThrowException() {
		when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookingManagementService.confirmBooking(BOOKING_ID))
				.isInstanceOf(BookingNotFoundException.class);
	}

	@Test
	void confirmBooking_WhenNotPending_ShouldThrowException() {
		testBooking.setStatus(BookingStatus.CONFIRMED);

		when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(testBooking));

		assertThatThrownBy(() -> bookingManagementService.confirmBooking(BOOKING_ID))
				.isInstanceOf(BookingOperationException.class);
	}

	@Test
	void getAvailableBonusPointsForBooking_Success() {
		BigDecimal totalPrice = new BigDecimal("500.00");
		when(bonusService.getAvailablePoints(USER_ID, totalPrice)).thenReturn(150);

		Integer result = bookingManagementService.getAvailableBonusPointsForBooking(USER_ID, totalPrice);

		assertThat(result).isEqualTo(150);
	}

	@Test
	void cancelBooking_WithBonusPointsRefund() {
		testBooking.setStatus(BookingStatus.PENDING);
		testBooking.setBonusPointsUsed(100);

		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));
		when(bookingValidator.canBookingBeCancelled(testBooking)).thenReturn(true);
		when(bookingRepository.save(testBooking)).thenReturn(testBooking);

		bookingManagementService.cancelBooking(BOOKING_ID, testUser);

		assertThat(testBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
		verify(bonusService).refundPoints(testBooking);
	}

	@Test
	void cancelBooking_WithoutBonusPoints_NoRefund() {
		testBooking.setStatus(BookingStatus.PENDING);
		testBooking.setBonusPointsUsed(0);

		when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(testBooking));
		when(bookingValidator.canBookingBeCancelled(testBooking)).thenReturn(true);
		when(bookingRepository.save(testBooking)).thenReturn(testBooking);

		bookingManagementService.cancelBooking(BOOKING_ID, testUser);

		assertThat(testBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
		verify(bonusService, never()).refundPoints(any());
	}
}