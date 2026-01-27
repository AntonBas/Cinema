package ua.lviv.bas.cinema.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

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
import ua.lviv.bas.cinema.security.CustomUserDetails;
import ua.lviv.bas.cinema.service.booking.ControllerFacade;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {

	@Mock
	private ControllerFacade controllerFacade;

	@InjectMocks
	private BookingController bookingController;

	private User createUser(Long id, String email) {
		User user = new User();
		user.setId(id);
		user.setEmail(email);
		return user;
	}

	private CustomUserDetails createCustomUserDetails(User user) {
		return new CustomUserDetails(user);
	}

	private BookingResponse createBookingResponse(Long id) {
		return BookingResponse.builder().id(id).bookingNumber("BK-20240115-00123").status(BookingStatus.PENDING)
				.sessionTime(LocalDateTime.now().plusDays(1)).movieTitle("Test Movie").hallName("Hall A")
				.totalPrice(new BigDecimal("150.00")).bonusPointsUsed(0).bonusDiscountAmount(BigDecimal.ZERO)
				.finalPrice(new BigDecimal("150.00")).liqpayOrderId("ORDER_ABC123")
				.expiresAt(LocalDateTime.now().plusMinutes(15)).createdAt(LocalDateTime.now()).build();
	}

	@Test
	void createBooking_ShouldCreateSuccessfully() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		BookingCreateRequest request = new BookingCreateRequest();
		request.setSessionId(100L);

		BookingResponse bookingResponse = createBookingResponse(1L);

		when(controllerFacade.createBooking(any(BookingCreateRequest.class), any(User.class)))
				.thenReturn(bookingResponse);

		ResponseEntity<BookingResponse> response = bookingController.createBooking(request, userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getId()).isEqualTo(1L);
	}

	@Test
	void getBooking_ShouldReturnBooking() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		Long bookingId = 1L;

		BookingResponse bookingResponse = createBookingResponse(bookingId);

		when(controllerFacade.getBookingById(bookingId, user)).thenReturn(bookingResponse);

		ResponseEntity<BookingResponse> response = bookingController.getBooking(bookingId, userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getId()).isEqualTo(bookingId);
	}

	@Test
	void getUserBookings_ShouldReturnBookings() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		Pageable pageable = PageRequest.of(0, 20);

		BookingResponse booking1 = createBookingResponse(1L);
		BookingResponse booking2 = createBookingResponse(2L);
		Page<BookingResponse> page = new PageImpl<>(Arrays.asList(booking1, booking2), pageable, 2);

		when(controllerFacade.getUserBookings(user.getId(), null, pageable)).thenReturn(page);

		ResponseEntity<Page<BookingResponse>> response = bookingController.getUserBookings(pageable, null, userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getTotalElements()).isEqualTo(2);
	}

	@Test
	void cancelBooking_ShouldCancelSuccessfully() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = createCustomUserDetails(user);
		Long bookingId = 1L;

		ResponseEntity<Void> response = bookingController.cancelBooking(bookingId, userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}
}