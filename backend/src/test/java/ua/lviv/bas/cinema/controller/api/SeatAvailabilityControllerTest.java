package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatReservationResponse;
import ua.lviv.bas.cinema.service.booking.availability.SeatReservationService;

@ExtendWith(MockitoExtension.class)
public class SeatAvailabilityControllerTest {

	@Mock
	private SeatReservationService seatAvailabilityService;

	@InjectMocks
	private SeatAvailabilityController seatAvailabilityController;

	private SeatReservationResponse createSeatAvailabilityResponse(Long sessionId) {
		SeatReservationResponse.SeatInfo seat1 = SeatReservationResponse.SeatInfo.builder().id(1L).row(1)
				.seatNumber(1).seatType("STANDARD").available(true).temporarilyReserved(false)
				.ticketPrices(Arrays.asList(SeatReservationResponse.TicketPriceInfo.builder().ticketTypeId(1L)
						.ticketTypeName("Adult").finalPrice(new BigDecimal("250.00")).build()))
				.build();

		return SeatReservationResponse.builder().sessionId(sessionId).movieTitle("Inception")
				.basePrice(new BigDecimal("200.00")).hallName("Hall A").availableSeats(75).seats(Arrays.asList(seat1))
				.build();
	}

	@Test
	void getSeatAvailability_ShouldReturnAvailabilitySuccessfully() {
		Long sessionId = 1L;
		SeatReservationResponse availabilityResponse = createSeatAvailabilityResponse(sessionId);

		when(seatAvailabilityService.getSeatAvailability(sessionId)).thenReturn(availabilityResponse);

		ResponseEntity<SeatReservationResponse> response = seatAvailabilityController.getSeatAvailability(sessionId);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(sessionId, response.getBody().getSessionId());
		assertEquals("Inception", response.getBody().getMovieTitle());
		assertEquals(75, response.getBody().getAvailableSeats());
	}
}