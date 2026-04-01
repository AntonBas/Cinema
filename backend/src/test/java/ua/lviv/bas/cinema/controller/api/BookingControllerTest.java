package ua.lviv.bas.cinema.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.config.security.user.CustomUserDetails;
import ua.lviv.bas.cinema.domain.booking.status.BookingStatus;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.booking.request.BookingCreateRequest;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;
import ua.lviv.bas.cinema.service.booking.ControllerFacade;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {

	@Mock
	private ControllerFacade controllerFacade;
	@InjectMocks
	private BookingController bookingController;

	private final Long USER_ID = 1L;
	private final String EMAIL = "user@example.com";
	private final Long BOOKING_ID = 1L;
	private final Long SESSION_ID = 100L;

	private User createUser() {
		User user = new User();
		user.setId(USER_ID);
		user.setEmail(EMAIL);
		return user;
	}

	private CustomUserDetails createUserDetails() {
		return new CustomUserDetails(createUser());
	}

	private BookingResponse createBookingResponse() {
		return new BookingResponse(BOOKING_ID, "BK-20240115-00123", BookingStatus.PENDING, SESSION_ID,
				LocalDateTime.now().plusDays(1), "Test Movie", "Hall A", new BigDecimal("150.00"), null,
				BigDecimal.ZERO, new BigDecimal("150.00"), null, LocalDateTime.now().plusMinutes(15),
				LocalDateTime.now(), null);
	}

	@Test
	void createBooking_ReturnsCreated() {
		User user = createUser();
		CustomUserDetails userDetails = createUserDetails();
		BookingCreateRequest request = new BookingCreateRequest(SESSION_ID, null, null);
		BookingResponse response = createBookingResponse();

		when(controllerFacade.createBooking(any(BookingCreateRequest.class), any(User.class))).thenReturn(response);

		ResponseEntity<BookingResponse> result = bookingController.createBooking(request, userDetails);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().id()).isEqualTo(BOOKING_ID);
		verify(controllerFacade).createBooking(request, user);
	}

	@Test
	void getBooking_ReturnsOk() {
		User user = createUser();
		CustomUserDetails userDetails = createUserDetails();
		BookingResponse response = createBookingResponse();

		when(controllerFacade.getBookingById(BOOKING_ID, user)).thenReturn(response);

		ResponseEntity<BookingResponse> result = bookingController.getBooking(BOOKING_ID, userDetails);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().id()).isEqualTo(BOOKING_ID);
		verify(controllerFacade).getBookingById(BOOKING_ID, user);
	}

	@Test
	void cancelBooking_ReturnsNoContent() {
		User user = createUser();
		CustomUserDetails userDetails = createUserDetails();

		ResponseEntity<Void> result = bookingController.cancelBooking(BOOKING_ID, userDetails);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		verify(controllerFacade).cancelBooking(BOOKING_ID, user);
	}
}