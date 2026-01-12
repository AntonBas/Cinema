package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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

import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatAvailabilityResponse;
import ua.lviv.bas.cinema.exception.domain.booking.SeatNotAvailableException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.service.booking.SeatAvailabilityService;

@ExtendWith(MockitoExtension.class)
public class SeatAvailabilityControllerTest {

	@Mock
	private SeatAvailabilityService seatAvailabilityService;

	@InjectMocks
	private SeatAvailabilityController seatAvailabilityController;

	private SeatAvailabilityResponse createSeatAvailabilityResponse(Long sessionId, String movieTitle,
			int availableSeats) {
		SeatAvailabilityResponse.SeatInfo seat1 = SeatAvailabilityResponse.SeatInfo.builder().id(1L).row(1)
				.seatNumber(1).seatType("STANDARD").available(true).temporarilyReserved(false)
				.ticketPrices(Arrays.asList(SeatAvailabilityResponse.TicketPriceInfo.builder().ticketTypeId(1L)
						.ticketTypeName("Adult").finalPrice(new BigDecimal("250.00")).build()))
				.build();

		SeatAvailabilityResponse.SeatInfo seat2 = SeatAvailabilityResponse.SeatInfo.builder().id(2L).row(1)
				.seatNumber(2).seatType("STANDARD").available(false).temporarilyReserved(false)
				.ticketPrices(Arrays.asList(SeatAvailabilityResponse.TicketPriceInfo.builder().ticketTypeId(1L)
						.ticketTypeName("Adult").finalPrice(new BigDecimal("250.00")).build()))
				.build();

		return SeatAvailabilityResponse.builder().sessionId(sessionId).movieTitle(movieTitle)
				.basePrice(new BigDecimal("200.00")).hallName("Hall A").availableSeats(availableSeats)
				.seats(Arrays.asList(seat1, seat2)).build();
	}

	@Test
	void getSeatAvailability_ShouldReturnAvailabilitySuccessfully() {
		Long sessionId = 1L;
		SeatAvailabilityResponse availabilityResponse = createSeatAvailabilityResponse(sessionId, "Inception", 75);

		when(seatAvailabilityService.getSeatAvailability(sessionId)).thenReturn(availabilityResponse);

		ResponseEntity<SeatAvailabilityResponse> response = seatAvailabilityController.getSeatAvailability(sessionId);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(sessionId, response.getBody().getSessionId());
		assertEquals("Inception", response.getBody().getMovieTitle());
		assertEquals(75, response.getBody().getAvailableSeats());
		verify(seatAvailabilityService).getSeatAvailability(sessionId);
	}

	@Test
	void getSeatAvailability_ShouldThrowException_WhenSessionNotFound() {
		Long sessionId = 999L;

		when(seatAvailabilityService.getSeatAvailability(sessionId)).thenThrow(new SessionNotFoundException(sessionId));

		assertThrows(SessionNotFoundException.class, () -> seatAvailabilityController.getSeatAvailability(sessionId));
		verify(seatAvailabilityService).getSeatAvailability(sessionId);
	}

	@Test
	void checkSeatAvailability_ShouldReturnOk_WhenSeatAvailable() {
		Long sessionId = 1L;
		Long seatId = 25L;

		doNothing().when(seatAvailabilityService).validateSeatAvailability(sessionId, seatId);

		ResponseEntity<Void> response = seatAvailabilityController.checkSeatAvailability(sessionId, seatId);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(seatAvailabilityService).validateSeatAvailability(sessionId, seatId);
	}

	@Test
	void checkSeatAvailability_ShouldThrowException_WhenSeatNotAvailable() {
		Long sessionId = 1L;
		Long seatId = 25L;

		doThrow(new SeatNotAvailableException("Seat not available")).when(seatAvailabilityService)
				.validateSeatAvailability(sessionId, seatId);

		assertThrows(SeatNotAvailableException.class,
				() -> seatAvailabilityController.checkSeatAvailability(sessionId, seatId));
		verify(seatAvailabilityService).validateSeatAvailability(sessionId, seatId);
	}

	@Test
	void checkSeatAvailability_ShouldThrowException_WhenSessionNotFound() {
		Long sessionId = 999L;
		Long seatId = 25L;

		doThrow(new SessionNotFoundException(sessionId)).when(seatAvailabilityService)
				.validateSeatAvailability(sessionId, seatId);

		assertThrows(SessionNotFoundException.class,
				() -> seatAvailabilityController.checkSeatAvailability(sessionId, seatId));
		verify(seatAvailabilityService).validateSeatAvailability(sessionId, seatId);
	}

	@Test
	void getAvailableSeatsCount_ShouldReturnCountSuccessfully() {
		Long sessionId = 1L;
		int availableSeatsCount = 75;

		when(seatAvailabilityService.getAvailableSeatsCount(sessionId)).thenReturn(availableSeatsCount);

		ResponseEntity<Integer> response = seatAvailabilityController.getAvailableSeatsCount(sessionId);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(75, response.getBody());
		verify(seatAvailabilityService).getAvailableSeatsCount(sessionId);
	}

	@Test
	void getAvailableSeatsCount_ShouldReturnZero_WhenNoAvailableSeats() {
		Long sessionId = 2L;
		int availableSeatsCount = 0;

		when(seatAvailabilityService.getAvailableSeatsCount(sessionId)).thenReturn(availableSeatsCount);

		ResponseEntity<Integer> response = seatAvailabilityController.getAvailableSeatsCount(sessionId);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(0, response.getBody());
		verify(seatAvailabilityService).getAvailableSeatsCount(sessionId);
	}

	@Test
	void getAvailableSeatsCount_ShouldThrowException_WhenSessionNotFound() {
		Long sessionId = 999L;

		when(seatAvailabilityService.getAvailableSeatsCount(sessionId))
				.thenThrow(new SessionNotFoundException(sessionId));

		assertThrows(SessionNotFoundException.class,
				() -> seatAvailabilityController.getAvailableSeatsCount(sessionId));
		verify(seatAvailabilityService).getAvailableSeatsCount(sessionId);
	}

	@Test
	void getSeatAvailability_ShouldReturnEmptySeatsList() {
		Long sessionId = 3L;

		SeatAvailabilityResponse availabilityResponse = SeatAvailabilityResponse.builder().sessionId(sessionId)
				.movieTitle("The Matrix").basePrice(new BigDecimal("300.00")).hallName("VIP Hall").availableSeats(0)
				.seats(Arrays.asList()).build();

		when(seatAvailabilityService.getSeatAvailability(sessionId)).thenReturn(availabilityResponse);

		ResponseEntity<SeatAvailabilityResponse> response = seatAvailabilityController.getSeatAvailability(sessionId);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(0, response.getBody().getAvailableSeats());
		verify(seatAvailabilityService).getSeatAvailability(sessionId);
	}

	@Test
	void checkSeatAvailability_ShouldHandleInvalidSessionIdFormat() {
		Long sessionId = -1L;
		Long seatId = 25L;

		doThrow(new IllegalArgumentException("Invalid session ID format")).when(seatAvailabilityService)
				.validateSeatAvailability(sessionId, seatId);

		assertThrows(IllegalArgumentException.class,
				() -> seatAvailabilityController.checkSeatAvailability(sessionId, seatId));
		verify(seatAvailabilityService).validateSeatAvailability(sessionId, seatId);
	}

	@Test
	void getAvailableSeatsCount_ShouldReturnFullHall() {
		Long sessionId = 4L;
		int availableSeatsCount = 100;

		when(seatAvailabilityService.getAvailableSeatsCount(sessionId)).thenReturn(availableSeatsCount);

		ResponseEntity<Integer> response = seatAvailabilityController.getAvailableSeatsCount(sessionId);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(100, response.getBody());
		verify(seatAvailabilityService).getAvailableSeatsCount(sessionId);
	}

	@Test
	void getSeatAvailability_ShouldReturnPartiallyBookedSession() {
		Long sessionId = 5L;
		SeatAvailabilityResponse availabilityResponse = createSeatAvailabilityResponse(sessionId, "Avatar", 45);

		when(seatAvailabilityService.getSeatAvailability(sessionId)).thenReturn(availabilityResponse);

		ResponseEntity<SeatAvailabilityResponse> response = seatAvailabilityController.getSeatAvailability(sessionId);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(45, response.getBody().getAvailableSeats());
		verify(seatAvailabilityService).getSeatAvailability(sessionId);
	}

	@Test
	void getSeatAvailability_ShouldHandleVIPSeats() {
		Long sessionId = 6L;

		SeatAvailabilityResponse.SeatInfo vipSeat = SeatAvailabilityResponse.SeatInfo.builder().id(101L).row(1)
				.seatNumber(1).seatType("VIP").available(true).temporarilyReserved(false)
				.ticketPrices(Arrays.asList(SeatAvailabilityResponse.TicketPriceInfo.builder().ticketTypeId(1L)
						.ticketTypeName("Adult").finalPrice(new BigDecimal("350.00")).build()))
				.build();

		SeatAvailabilityResponse availabilityResponse = SeatAvailabilityResponse.builder().sessionId(sessionId)
				.movieTitle("Avengers").basePrice(new BigDecimal("250.00")).hallName("Premium Hall").availableSeats(1)
				.seats(Arrays.asList(vipSeat)).build();

		when(seatAvailabilityService.getSeatAvailability(sessionId)).thenReturn(availabilityResponse);

		ResponseEntity<SeatAvailabilityResponse> response = seatAvailabilityController.getSeatAvailability(sessionId);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("VIP", response.getBody().getSeats().get(0).getSeatType());
		assertEquals(new BigDecimal("350.00"),
				response.getBody().getSeats().get(0).getTicketPrices().get(0).getFinalPrice());
		verify(seatAvailabilityService).getSeatAvailability(sessionId);
	}

	@Test
	void getSeatAvailability_ShouldHandleTemporarilyReservedSeats() {
		Long sessionId = 7L;

		SeatAvailabilityResponse.SeatInfo reservedSeat = SeatAvailabilityResponse.SeatInfo.builder().id(50L).row(5)
				.seatNumber(10).seatType("STANDARD").available(false).temporarilyReserved(true)
				.ticketPrices(Arrays.asList(SeatAvailabilityResponse.TicketPriceInfo.builder().ticketTypeId(1L)
						.ticketTypeName("Adult").finalPrice(new BigDecimal("250.00")).build()))
				.build();

		SeatAvailabilityResponse availabilityResponse = SeatAvailabilityResponse.builder().sessionId(sessionId)
				.movieTitle("Spider-Man").basePrice(new BigDecimal("200.00")).hallName("Hall B").availableSeats(0)
				.seats(Arrays.asList(reservedSeat)).build();

		when(seatAvailabilityService.getSeatAvailability(sessionId)).thenReturn(availabilityResponse);

		ResponseEntity<SeatAvailabilityResponse> response = seatAvailabilityController.getSeatAvailability(sessionId);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(true, response.getBody().getSeats().get(0).getTemporarilyReserved());
		verify(seatAvailabilityService).getSeatAvailability(sessionId);
	}
}