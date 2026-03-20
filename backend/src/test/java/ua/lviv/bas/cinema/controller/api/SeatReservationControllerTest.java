package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.booking.response.SeatReservationResponse;
import ua.lviv.bas.cinema.security.CustomUserDetails;
import ua.lviv.bas.cinema.service.booking.reservation.SeatReservationService;

@ExtendWith(MockitoExtension.class)
public class SeatReservationControllerTest {

	@Mock
	private SeatReservationService seatReservationService;

	@Mock
	private CustomUserDetails customUserDetails;

	@InjectMocks
	private SeatReservationController seatReservationController;

	private SeatReservationResponse createSeatAvailabilityResponse(Long sessionId) {
		SeatReservationResponse.TicketPriceInfo ticketPriceInfo = new SeatReservationResponse.TicketPriceInfo(1L,
				"Adult", new BigDecimal("250.00"));

		SeatReservationResponse.SeatInfo seat1 = new SeatReservationResponse.SeatInfo(1L, 1, 1, SeatType.STANDARD, true,
				false, true, Arrays.asList(ticketPriceInfo));

		return new SeatReservationResponse(sessionId, "Inception", new BigDecimal("200.00"), "Hall A", 75,
				Arrays.asList(seat1));
	}

	@Test
	void getSeatAvailability_ShouldReturnAvailabilitySuccessfully() {
		Long sessionId = 1L;
		SeatReservationResponse availabilityResponse = createSeatAvailabilityResponse(sessionId);

		when(seatReservationService.getSeatAvailability(sessionId)).thenReturn(availabilityResponse);

		ResponseEntity<SeatReservationResponse> response = seatReservationController.getSeatAvailability(sessionId);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(sessionId, response.getBody().sessionId());
		assertEquals("Inception", response.getBody().movieTitle());
		assertEquals(75, response.getBody().availableSeats());
	}

	@Test
	void temporaryHoldSeat_ShouldReturnOk_WhenHoldIsSuccessful() {
		Long sessionId = 1L;
		Long seatId = 10L;
		Long userId = 100L;
		User user = new User();
		user.setId(userId);

		when(customUserDetails.getUser()).thenReturn(user);
		doNothing().when(seatReservationService).temporaryHoldSeat(sessionId, seatId, user);

		ResponseEntity<Void> response = seatReservationController.temporaryHoldSeat(sessionId, seatId,
				customUserDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(seatReservationService).temporaryHoldSeat(sessionId, seatId, user);
	}
}