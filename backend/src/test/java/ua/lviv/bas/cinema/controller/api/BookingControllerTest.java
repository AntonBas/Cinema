package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;
import ua.lviv.bas.cinema.dto.booking.request.BookingCreateRequest;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.service.booking.BookingService;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {

	@Mock
	private BookingService bookingService;

	@InjectMocks
	private BookingController bookingController;

	private User createUser(Long id, String email) {
		User user = new User();
		user.setId(id);
		user.setEmail(email);
		user.setFirstName("John");
		user.setLastName("Doe");
		return user;
	}

	private BookingResponse createBookingResponse(Long id, String bookingNumber, BigDecimal totalPrice,
			BookingStatus bookingStatus, PaymentStatus paymentStatus, Integer bonusPointsUsed, String movieTitle) {
		BigDecimal bonusDiscount = bonusPointsUsed > 0
				? BigDecimal.valueOf(bonusPointsUsed / 100.0 * totalPrice.doubleValue())
				: BigDecimal.ZERO;
		BigDecimal finalPrice = totalPrice.subtract(bonusDiscount);

		return BookingResponse.builder().id(id).bookingNumber(bookingNumber).status(bookingStatus)
				.sessionTime(LocalDateTime.now().plusDays(1)).movieTitle(movieTitle != null ? movieTitle : "Test Movie")
				.hallName("Hall A").totalPrice(totalPrice).bonusPointsUsed(bonusPointsUsed)
				.bonusDiscountAmount(bonusDiscount).finalPrice(finalPrice).paymentStatus(paymentStatus)
				.liqpayOrderId("ORDER_ABC123").expiresAt(LocalDateTime.now().plusMinutes(15))
				.createdAt(LocalDateTime.now()).bookedSeats(Arrays.asList()).build();
	}

	@Test
	void createBooking_ShouldCreateSuccessfully() {
		User user = createUser(1L, "user@example.com");
		BookingCreateRequest request = new BookingCreateRequest();
		request.setSessionId(100L);
		request.setBonusPointsToUse(0);

		BookingResponse bookingResponse = createBookingResponse(1L, "BK-20240115-00123", new BigDecimal("150.00"),
				BookingStatus.PENDING, PaymentStatus.PENDING, 0, "Inception");

		when(bookingService.createBooking(request, user)).thenReturn(bookingResponse);

		ResponseEntity<BookingResponse> response = bookingController.createBooking(request, user);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(1L, response.getBody().getId());
		assertEquals("BK-20240115-00123", response.getBody().getBookingNumber());
		assertEquals(new BigDecimal("150.00"), response.getBody().getTotalPrice());
		verify(bookingService).createBooking(request, user);
	}

	@Test
	void createBooking_ShouldCreateWithBonusPoints() {
		User user = createUser(1L, "user@example.com");
		BookingCreateRequest request = new BookingCreateRequest();
		request.setSessionId(100L);
		request.setBonusPointsToUse(100);

		BookingResponse bookingResponse = createBookingResponse(1L, "BK-20240115-00124", new BigDecimal("150.00"),
				BookingStatus.PENDING, PaymentStatus.PENDING, 100, "The Matrix");

		when(bookingService.createBooking(request, user)).thenReturn(bookingResponse);

		ResponseEntity<BookingResponse> response = bookingController.createBooking(request, user);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(100, response.getBody().getBonusPointsUsed());
		verify(bookingService).createBooking(request, user);
	}

	@Test
	void createBooking_ShouldThrowException_WhenInvalidRequest() {
		User user = createUser(1L, "user@example.com");
		BookingCreateRequest request = new BookingCreateRequest();

		when(bookingService.createBooking(request, user)).thenThrow(new IllegalArgumentException("Invalid request"));

		assertThrows(IllegalArgumentException.class, () -> bookingController.createBooking(request, user));
		verify(bookingService).createBooking(request, user);
	}

	@Test
	void getBooking_ShouldReturnBooking_WhenExistsAndAuthorized() {
		User user = createUser(1L, "user@example.com");
		Long bookingId = 1L;

		BookingResponse bookingResponse = createBookingResponse(bookingId, "BK-20240115-00125",
				new BigDecimal("150.00"), BookingStatus.CONFIRMED, PaymentStatus.SUCCESS, 50, "Interstellar");

		when(bookingService.getBookingById(bookingId, user)).thenReturn(bookingResponse);

		ResponseEntity<BookingResponse> response = bookingController.getBooking(bookingId, user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(bookingId, response.getBody().getId());
		assertEquals(BookingStatus.CONFIRMED, response.getBody().getStatus());
		verify(bookingService).getBookingById(bookingId, user);
	}

	@Test
	void getBooking_ShouldThrowException_WhenNotFound() {
		User user = createUser(1L, "user@example.com");
		Long bookingId = 999L;

		when(bookingService.getBookingById(bookingId, user)).thenThrow(new BookingNotFoundException(bookingId));

		assertThrows(BookingNotFoundException.class, () -> bookingController.getBooking(bookingId, user));
		verify(bookingService).getBookingById(bookingId, user);
	}

	@Test
	void getUserBookings_ShouldReturnBookings_WhenNoStatusFilter() {
		User user = createUser(1L, "user@example.com");
		Pageable pageable = PageRequest.of(0, 20);

		BookingResponse booking1 = createBookingResponse(1L, "BK-20240115-00126", new BigDecimal("150.00"),
				BookingStatus.CONFIRMED, PaymentStatus.SUCCESS, 100, "Movie 1");
		BookingResponse booking2 = createBookingResponse(2L, "BK-20240115-00127", new BigDecimal("200.00"),
				BookingStatus.EXPIRED, PaymentStatus.SUCCESS, 0, "Movie 2");
		List<BookingResponse> bookings = Arrays.asList(booking1, booking2);
		Page<BookingResponse> page = new PageImpl<>(bookings, pageable, 2);

		when(bookingService.getUserBookings(user.getId(), null, pageable)).thenReturn(page);

		ResponseEntity<Page<BookingResponse>> response = bookingController.getUserBookings(pageable, null, user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().getTotalElements());
		assertEquals(2, response.getBody().getContent().size());
		verify(bookingService).getUserBookings(user.getId(), null, pageable);
	}

	@Test
	void getUserBookings_ShouldReturnFilteredBookings_WhenStatusFiltered() {
		User user = createUser(1L, "user@example.com");
		Pageable pageable = PageRequest.of(0, 20);
		BookingStatus status = BookingStatus.CONFIRMED;

		BookingResponse booking1 = createBookingResponse(1L, "BK-20240115-00128", new BigDecimal("150.00"), status,
				PaymentStatus.SUCCESS, 100, "Movie 1");
		BookingResponse booking2 = createBookingResponse(2L, "BK-20240115-00129", new BigDecimal("200.00"), status,
				PaymentStatus.SUCCESS, 50, "Movie 2");
		List<BookingResponse> bookings = Arrays.asList(booking1, booking2);
		Page<BookingResponse> page = new PageImpl<>(bookings, pageable, 2);

		when(bookingService.getUserBookings(user.getId(), status, pageable)).thenReturn(page);

		ResponseEntity<Page<BookingResponse>> response = bookingController.getUserBookings(pageable, status, user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().getTotalElements());
		response.getBody().getContent().forEach(booking -> assertEquals(status, booking.getStatus()));
		verify(bookingService).getUserBookings(user.getId(), status, pageable);
	}

	@Test
	void getUserBookings_ShouldReturnEmptyPage_WhenNoBookings() {
		User user = createUser(1L, "user@example.com");
		Pageable pageable = PageRequest.of(0, 20);

		Page<BookingResponse> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);

		when(bookingService.getUserBookings(user.getId(), null, pageable)).thenReturn(emptyPage);

		ResponseEntity<Page<BookingResponse>> response = bookingController.getUserBookings(pageable, null, user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(0, response.getBody().getTotalElements());
		assertEquals(0, response.getBody().getContent().size());
		verify(bookingService).getUserBookings(user.getId(), null, pageable);
	}

	@Test
	void cancelBooking_ShouldCancelSuccessfully() {
		User user = createUser(1L, "user@example.com");
		Long bookingId = 1L;

		doNothing().when(bookingService).cancelBooking(bookingId, user);

		ResponseEntity<Void> response = bookingController.cancelBooking(bookingId, user);

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		verify(bookingService).cancelBooking(bookingId, user);
	}

	@Test
	void cancelBooking_ShouldThrowException_WhenCannotCancel() {
		User user = createUser(1L, "user@example.com");
		Long bookingId = 1L;

		doThrow(new RuntimeException("Cannot cancel booking")).when(bookingService).cancelBooking(bookingId, user);

		assertThrows(RuntimeException.class, () -> bookingController.cancelBooking(bookingId, user));
		verify(bookingService).cancelBooking(bookingId, user);
	}

	@Test
	void cancelBooking_ShouldThrowException_WhenNotFound() {
		User user = createUser(1L, "user@example.com");
		Long bookingId = 999L;

		doThrow(new BookingNotFoundException(bookingId)).when(bookingService).cancelBooking(bookingId, user);

		assertThrows(BookingNotFoundException.class, () -> bookingController.cancelBooking(bookingId, user));
		verify(bookingService).cancelBooking(bookingId, user);
	}

	@Test
	void getAvailableBonusPoints_ShouldReturnPoints() {
		User user = createUser(1L, "user@example.com");
		BigDecimal totalPrice = new BigDecimal("200.00");
		Integer availablePoints = 100;

		when(bookingService.getAvailableBonusPointsForBooking(user.getId(), totalPrice)).thenReturn(availablePoints);

		ResponseEntity<Integer> response = bookingController.getAvailableBonusPoints(totalPrice, user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(100, response.getBody());
		verify(bookingService).getAvailableBonusPointsForBooking(user.getId(), totalPrice);
	}

	@Test
	void getAvailableBonusPoints_ShouldReturnZero_WhenNoPointsAvailable() {
		User user = createUser(1L, "user@example.com");
		BigDecimal totalPrice = new BigDecimal("50.00");
		Integer availablePoints = 0;

		when(bookingService.getAvailableBonusPointsForBooking(user.getId(), totalPrice)).thenReturn(availablePoints);

		ResponseEntity<Integer> response = bookingController.getAvailableBonusPoints(totalPrice, user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(0, response.getBody());
		verify(bookingService).getAvailableBonusPointsForBooking(user.getId(), totalPrice);
	}

	@Test
	void getUserBookings_ShouldHandleDifferentPageSizes() {
		User user = createUser(1L, "user@example.com");
		Pageable pageable = PageRequest.of(1, 5);

		Page<BookingResponse> page = new PageImpl<>(Arrays.asList(), pageable, 0);

		when(bookingService.getUserBookings(user.getId(), null, pageable)).thenReturn(page);

		ResponseEntity<Page<BookingResponse>> response = bookingController.getUserBookings(pageable, null, user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(0, response.getBody().getTotalElements());
		verify(bookingService).getUserBookings(user.getId(), null, pageable);
	}

	@Test
	void createBooking_ShouldHandleSessionNotAvailable() {
		User user = createUser(1L, "user@example.com");
		BookingCreateRequest request = new BookingCreateRequest();
		request.setSessionId(999L);
		request.setBonusPointsToUse(0);

		when(bookingService.createBooking(request, user)).thenThrow(new RuntimeException("Session not available"));

		assertThrows(RuntimeException.class, () -> bookingController.createBooking(request, user));
		verify(bookingService).createBooking(request, user);
	}

	@Test
	void getAvailableBonusPoints_ShouldHandleMaximumBonusPoints() {
		User user = createUser(1L, "user@example.com");
		BigDecimal totalPrice = new BigDecimal("1000.00");
		Integer availablePoints = 500;

		when(bookingService.getAvailableBonusPointsForBooking(user.getId(), totalPrice)).thenReturn(availablePoints);

		ResponseEntity<Integer> response = bookingController.getAvailableBonusPoints(totalPrice, user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(500, response.getBody());
		verify(bookingService).getAvailableBonusPointsForBooking(user.getId(), totalPrice);
	}

	@Test
	void createBooking_ShouldHandleBookingWithNoSeats() {
		User user = createUser(1L, "user@example.com");
		BookingCreateRequest request = new BookingCreateRequest();
		request.setSessionId(100L);
		request.setBonusPointsToUse(0);

		when(bookingService.createBooking(request, user))
				.thenThrow(new IllegalArgumentException("At least one seat must be selected"));

		assertThrows(IllegalArgumentException.class, () -> bookingController.createBooking(request, user));
		verify(bookingService).createBooking(request, user);
	}

	@Test
	void getUserBookings_ShouldHandleStatusFilterNotFound() {
		User user = createUser(1L, "user@example.com");
		Pageable pageable = PageRequest.of(0, 20);
		BookingStatus status = BookingStatus.CANCELLED;

		Page<BookingResponse> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);

		when(bookingService.getUserBookings(user.getId(), status, pageable)).thenReturn(emptyPage);

		ResponseEntity<Page<BookingResponse>> response = bookingController.getUserBookings(pageable, status, user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(0, response.getBody().getTotalElements());
		verify(bookingService).getUserBookings(user.getId(), status, pageable);
	}
}