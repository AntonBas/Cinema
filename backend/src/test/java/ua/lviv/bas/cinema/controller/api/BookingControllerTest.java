package ua.lviv.bas.cinema.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
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
import ua.lviv.bas.cinema.dto.booking.request.BookingCreateRequest;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.security.CustomUserDetails;
import ua.lviv.bas.cinema.service.booking.BookingService;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

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

	private CustomUserDetails createCustomUserDetails(User user) {
		return new CustomUserDetails(user);
	}

	private BookingResponse createBookingResponse(Long id, String bookingNumber, BigDecimal totalPrice,
			BookingStatus bookingStatus, Integer bonusPointsUsed, String movieTitle) {
		BigDecimal bonusDiscount = bonusPointsUsed > 0
				? BigDecimal.valueOf(bonusPointsUsed / 100.0 * totalPrice.doubleValue())
				: BigDecimal.ZERO;
		BigDecimal finalPrice = totalPrice.subtract(bonusDiscount);

		return BookingResponse.builder().id(id).bookingNumber(bookingNumber).status(bookingStatus)
				.sessionTime(LocalDateTime.now().plusDays(1)).movieTitle(movieTitle != null ? movieTitle : "Test Movie")
				.hallName("Hall A").totalPrice(totalPrice).bonusPointsUsed(bonusPointsUsed)
				.bonusDiscountAmount(bonusDiscount).finalPrice(finalPrice).liqpayOrderId("ORDER_ABC123")
				.expiresAt(LocalDateTime.now().plusMinutes(15)).createdAt(LocalDateTime.now())
				.bookedSeats(Arrays.asList()).build();
	}

	@Test
	void createBooking_ShouldCreateSuccessfully() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		BookingCreateRequest request = new BookingCreateRequest();
		request.setSessionId(100L);
		request.setBonusPointsToUse(0);

		BookingResponse bookingResponse = createBookingResponse(1L, "BK-20240115-00123", new BigDecimal("150.00"),
				BookingStatus.PENDING, 0, "Inception");

		when(bookingService.createBooking(request, user)).thenReturn(bookingResponse);

		ResponseEntity<BookingResponse> response = bookingController.createBooking(request, userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getId()).isEqualTo(1L);
		assertThat(response.getBody().getBookingNumber()).isEqualTo("BK-20240115-00123");
		assertThat(response.getBody().getTotalPrice()).isEqualTo(new BigDecimal("150.00"));
		verify(bookingService).createBooking(request, user);
	}

	@Test
	void createBooking_ShouldCreateWithBonusPoints() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		BookingCreateRequest request = new BookingCreateRequest();
		request.setSessionId(100L);
		request.setBonusPointsToUse(100);

		BookingResponse bookingResponse = createBookingResponse(1L, "BK-20240115-00124", new BigDecimal("150.00"),
				BookingStatus.PENDING, 100, "The Matrix");

		when(bookingService.createBooking(request, user)).thenReturn(bookingResponse);

		ResponseEntity<BookingResponse> response = bookingController.createBooking(request, userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getBonusPointsUsed()).isEqualTo(100);
		verify(bookingService).createBooking(request, user);
	}

	@Test
	void createBooking_ShouldThrowException_WhenInvalidRequest() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		BookingCreateRequest request = new BookingCreateRequest();

		when(bookingService.createBooking(request, user)).thenThrow(new IllegalArgumentException("Invalid request"));

		assertThrows(IllegalArgumentException.class, () -> bookingController.createBooking(request, userDetails));
		verify(bookingService).createBooking(request, user);
	}

	@Test
	void getBooking_ShouldReturnBooking_WhenExistsAndAuthorized() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		Long bookingId = 1L;

		BookingResponse bookingResponse = createBookingResponse(bookingId, "BK-20240115-00125",
				new BigDecimal("150.00"), BookingStatus.CONFIRMED, 50, "Interstellar");

		when(bookingService.getBookingById(bookingId, user)).thenReturn(bookingResponse);

		ResponseEntity<BookingResponse> response = bookingController.getBooking(bookingId, userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getId()).isEqualTo(bookingId);
		assertThat(response.getBody().getStatus()).isEqualTo(BookingStatus.CONFIRMED);
		verify(bookingService).getBookingById(bookingId, user);
	}

	@Test
	void getBooking_ShouldThrowException_WhenNotFound() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		Long bookingId = 999L;

		when(bookingService.getBookingById(bookingId, user)).thenThrow(new BookingNotFoundException(bookingId));

		assertThrows(BookingNotFoundException.class, () -> bookingController.getBooking(bookingId, userDetails));
		verify(bookingService).getBookingById(bookingId, user);
	}

	@Test
	void getUserBookings_ShouldReturnBookings_WhenNoStatusFilter() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		Pageable pageable = PageRequest.of(0, 20);

		BookingResponse booking1 = createBookingResponse(1L, "BK-20240115-00126", new BigDecimal("150.00"),
				BookingStatus.CONFIRMED, 100, "Movie 1");
		BookingResponse booking2 = createBookingResponse(2L, "BK-20240115-00127", new BigDecimal("200.00"),
				BookingStatus.EXPIRED, 0, "Movie 2");
		List<BookingResponse> bookings = Arrays.asList(booking1, booking2);
		Page<BookingResponse> page = new PageImpl<>(bookings, pageable, 2);

		when(bookingService.getUserBookings(user.getId(), null, pageable)).thenReturn(page);

		ResponseEntity<Page<BookingResponse>> response = bookingController.getUserBookings(pageable, null, userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getTotalElements()).isEqualTo(2);
		assertThat(response.getBody().getContent()).hasSize(2);
		verify(bookingService).getUserBookings(user.getId(), null, pageable);
	}

	@Test
	void getUserBookings_ShouldReturnFilteredBookings_WhenStatusFiltered() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		Pageable pageable = PageRequest.of(0, 20);
		BookingStatus status = BookingStatus.CONFIRMED;

		BookingResponse booking1 = createBookingResponse(1L, "BK-20240115-00128", new BigDecimal("150.00"), status, 100,
				"Movie 1");
		BookingResponse booking2 = createBookingResponse(2L, "BK-20240115-00129", new BigDecimal("200.00"), status, 50,
				"Movie 2");
		List<BookingResponse> bookings = Arrays.asList(booking1, booking2);
		Page<BookingResponse> page = new PageImpl<>(bookings, pageable, 2);

		when(bookingService.getUserBookings(user.getId(), status, pageable)).thenReturn(page);

		ResponseEntity<Page<BookingResponse>> response = bookingController.getUserBookings(pageable, status,
				userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getTotalElements()).isEqualTo(2);
		response.getBody().getContent().forEach(booking -> assertThat(booking.getStatus()).isEqualTo(status));
		verify(bookingService).getUserBookings(user.getId(), status, pageable);
	}

	@Test
	void getUserBookings_ShouldReturnEmptyPage_WhenNoBookings() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		Pageable pageable = PageRequest.of(0, 20);

		Page<BookingResponse> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);

		when(bookingService.getUserBookings(user.getId(), null, pageable)).thenReturn(emptyPage);

		ResponseEntity<Page<BookingResponse>> response = bookingController.getUserBookings(pageable, null, userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getTotalElements()).isEqualTo(0);
		assertThat(response.getBody().getContent()).isEmpty();
		verify(bookingService).getUserBookings(user.getId(), null, pageable);
	}

	@Test
	void cancelBooking_ShouldCancelSuccessfully() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		Long bookingId = 1L;

		doNothing().when(bookingService).cancelBooking(bookingId, user);

		ResponseEntity<Void> response = bookingController.cancelBooking(bookingId, userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		verify(bookingService).cancelBooking(bookingId, user);
	}

	@Test
	void cancelBooking_ShouldThrowException_WhenCannotCancel() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		Long bookingId = 1L;

		doThrow(new RuntimeException("Cannot cancel booking")).when(bookingService).cancelBooking(bookingId, user);

		assertThrows(RuntimeException.class, () -> bookingController.cancelBooking(bookingId, userDetails));
		verify(bookingService).cancelBooking(bookingId, user);
	}

	@Test
	void cancelBooking_ShouldThrowException_WhenNotFound() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		Long bookingId = 999L;

		doThrow(new BookingNotFoundException(bookingId)).when(bookingService).cancelBooking(bookingId, user);

		assertThrows(BookingNotFoundException.class, () -> bookingController.cancelBooking(bookingId, userDetails));
		verify(bookingService).cancelBooking(bookingId, user);
	}

	@Test
	void getAvailableBonusPoints_ShouldReturnPoints() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		BigDecimal totalPrice = new BigDecimal("200.00");
		Integer availablePoints = 100;

		when(bookingService.getAvailableBonusPointsForBooking(user.getId(), totalPrice)).thenReturn(availablePoints);

		ResponseEntity<Integer> response = bookingController.getAvailableBonusPoints(totalPrice, userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(100);
		verify(bookingService).getAvailableBonusPointsForBooking(user.getId(), totalPrice);
	}

	@Test
	void getAvailableBonusPoints_ShouldReturnZero_WhenNoPointsAvailable() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		BigDecimal totalPrice = new BigDecimal("50.00");
		Integer availablePoints = 0;

		when(bookingService.getAvailableBonusPointsForBooking(user.getId(), totalPrice)).thenReturn(availablePoints);

		ResponseEntity<Integer> response = bookingController.getAvailableBonusPoints(totalPrice, userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(0);
		verify(bookingService).getAvailableBonusPointsForBooking(user.getId(), totalPrice);
	}

	@Test
	void getUserBookings_ShouldHandleDifferentPageSizes() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		Pageable pageable = PageRequest.of(1, 5);

		Page<BookingResponse> page = new PageImpl<>(Arrays.asList(), pageable, 0);

		when(bookingService.getUserBookings(user.getId(), null, pageable)).thenReturn(page);

		ResponseEntity<Page<BookingResponse>> response = bookingController.getUserBookings(pageable, null, userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getTotalElements()).isEqualTo(0);
		verify(bookingService).getUserBookings(user.getId(), null, pageable);
	}

	@Test
	void createBooking_ShouldHandleSessionNotAvailable() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		BookingCreateRequest request = new BookingCreateRequest();
		request.setSessionId(999L);
		request.setBonusPointsToUse(0);

		when(bookingService.createBooking(request, user)).thenThrow(new RuntimeException("Session not available"));

		assertThrows(RuntimeException.class, () -> bookingController.createBooking(request, userDetails));
		verify(bookingService).createBooking(request, user);
	}

	@Test
	void getAvailableBonusPoints_ShouldHandleMaximumBonusPoints() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		BigDecimal totalPrice = new BigDecimal("1000.00");
		Integer availablePoints = 500;

		when(bookingService.getAvailableBonusPointsForBooking(user.getId(), totalPrice)).thenReturn(availablePoints);

		ResponseEntity<Integer> response = bookingController.getAvailableBonusPoints(totalPrice, userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(500);
		verify(bookingService).getAvailableBonusPointsForBooking(user.getId(), totalPrice);
	}

	@Test
	void createBooking_ShouldHandleBookingWithNoSeats() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		BookingCreateRequest request = new BookingCreateRequest();
		request.setSessionId(100L);
		request.setBonusPointsToUse(0);

		when(bookingService.createBooking(request, user))
				.thenThrow(new IllegalArgumentException("At least one seat must be selected"));

		assertThrows(IllegalArgumentException.class, () -> bookingController.createBooking(request, userDetails));
		verify(bookingService).createBooking(request, user);
	}

	@Test
	void getUserBookings_ShouldHandleStatusFilterNotFound() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		Pageable pageable = PageRequest.of(0, 20);
		BookingStatus status = BookingStatus.CANCELLED;

		Page<BookingResponse> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);

		when(bookingService.getUserBookings(user.getId(), status, pageable)).thenReturn(emptyPage);

		ResponseEntity<Page<BookingResponse>> response = bookingController.getUserBookings(pageable, status,
				userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getTotalElements()).isEqualTo(0);
		verify(bookingService).getUserBookings(user.getId(), status, pageable);
	}

	@Test
	void getBooking_ShouldHandleUnauthorizedAccess() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		Long bookingId = 1L;

		when(bookingService.getBookingById(bookingId, user))
				.thenThrow(new RuntimeException("Access denied to booking"));

		assertThrows(RuntimeException.class, () -> bookingController.getBooking(bookingId, userDetails));
		verify(bookingService).getBookingById(bookingId, user);
	}

	@Test
	void createBooking_ShouldHandleSeatNotAvailable() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		BookingCreateRequest request = new BookingCreateRequest();
		request.setSessionId(100L);
		request.setBonusPointsToUse(0);

		when(bookingService.createBooking(request, user))
				.thenThrow(new RuntimeException("Selected seat not available"));

		assertThrows(RuntimeException.class, () -> bookingController.createBooking(request, userDetails));
		verify(bookingService).createBooking(request, user);
	}

	@Test
	void getAvailableBonusPoints_ShouldHandleException() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		BigDecimal totalPrice = new BigDecimal("200.00");

		when(bookingService.getAvailableBonusPointsForBooking(user.getId(), totalPrice))
				.thenThrow(new RuntimeException("Error calculating bonus points"));

		assertThrows(RuntimeException.class, () -> bookingController.getAvailableBonusPoints(totalPrice, userDetails));
		verify(bookingService).getAvailableBonusPointsForBooking(user.getId(), totalPrice);
	}

	@Test
	void cancelBooking_ShouldHandleBookingAlreadyCancelled() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		Long bookingId = 1L;

		doThrow(new RuntimeException("Booking already cancelled")).when(bookingService).cancelBooking(bookingId, user);

		assertThrows(RuntimeException.class, () -> bookingController.cancelBooking(bookingId, userDetails));
		verify(bookingService).cancelBooking(bookingId, user);
	}

	@Test
	void getUserBookings_ShouldHandleInternalError() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		Pageable pageable = PageRequest.of(0, 20);

		when(bookingService.getUserBookings(user.getId(), null, pageable))
				.thenThrow(new RuntimeException("Database connection error"));

		assertThrows(RuntimeException.class, () -> bookingController.getUserBookings(pageable, null, userDetails));
		verify(bookingService).getUserBookings(user.getId(), null, pageable);
	}

	@Test
	void createBooking_ShouldHandleZeroBonusPoints() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		BookingCreateRequest request = new BookingCreateRequest();
		request.setSessionId(100L);
		request.setBonusPointsToUse(0);

		BookingResponse bookingResponse = createBookingResponse(1L, "BK-20240115-00130", new BigDecimal("150.00"),
				BookingStatus.PENDING, 0, "Movie");

		when(bookingService.createBooking(request, user)).thenReturn(bookingResponse);

		ResponseEntity<BookingResponse> response = bookingController.createBooking(request, userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getBonusPointsUsed()).isEqualTo(0);
		verify(bookingService).createBooking(request, user);
	}

	@Test
	void getUserBookings_ShouldHandleLargePageSize() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		Pageable pageable = PageRequest.of(0, 100);

		Page<BookingResponse> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);

		when(bookingService.getUserBookings(user.getId(), null, pageable)).thenReturn(emptyPage);

		ResponseEntity<Page<BookingResponse>> response = bookingController.getUserBookings(pageable, null, userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		verify(bookingService).getUserBookings(user.getId(), null, pageable);
	}
}