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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

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

	@Mock
	private Authentication authentication;

	@Mock
	private SecurityContext securityContext;

	@InjectMocks
	private SeatReservationController seatReservationController;

	private SeatReservationResponse createSeatAvailabilityResponse(Long sessionId) {
		SeatReservationResponse.SeatInfo seat1 = SeatReservationResponse.SeatInfo
				.builder().id(1L).row(1).seatNumber(1).seatType(SeatType.STANDARD).available(true)
				.temporarilyReserved(false).ticketPrices(Arrays.asList(SeatReservationResponse.TicketPriceInfo.builder()
						.ticketTypeId(1L).ticketTypeName("Adult").finalPrice(new BigDecimal("250.00")).build()))
				.build();

		return SeatReservationResponse.builder().sessionId(sessionId).movieTitle("Inception")
				.basePrice(new BigDecimal("200.00")).hallName("Hall A").availableSeats(75).seats(Arrays.asList(seat1))
				.build();
	}

	@Test
	void getSeatAvailability_ShouldReturnAvailabilitySuccessfully() {
		Long sessionId = 1L;
		SeatReservationResponse availabilityResponse = createSeatAvailabilityResponse(sessionId);

		when(seatReservationService.getSeatAvailability(sessionId)).thenReturn(availabilityResponse);

		ResponseEntity<SeatReservationResponse> response = seatReservationController.getSeatAvailability(sessionId);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(sessionId, response.getBody().getSessionId());
		assertEquals("Inception", response.getBody().getMovieTitle());
		assertEquals(75, response.getBody().getAvailableSeats());
	}

	@Test
	void temporaryHoldSeat_ShouldReturnOk_WhenHoldIsSuccessful() {
		Long sessionId = 1L;
		Long seatId = 10L;
		Long userId = 100L;
		User user = new User();
		user.setId(userId);

		when(customUserDetails.getUserId()).thenReturn(userId);
		when(customUserDetails.getUser()).thenReturn(user);
		doNothing().when(seatReservationService).temporaryHoldSeat(sessionId, seatId, user);

		ResponseEntity<Void> response = seatReservationController.temporaryHoldSeat(sessionId, seatId,
				customUserDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(seatReservationService).temporaryHoldSeat(sessionId, seatId, user);
	}
}