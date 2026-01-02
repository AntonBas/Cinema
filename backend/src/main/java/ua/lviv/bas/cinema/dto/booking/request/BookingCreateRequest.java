package ua.lviv.bas.cinema.dto.booking.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new booking")
public class BookingCreateRequest {

	@NotNull(message = "Session ID is required")
	@Schema(description = "ID of the cinema session", example = "1")
	private Long sessionId;

	@NotNull(message = "At least one seat must be selected")
	@Size(min = 1, max = 10, message = "You can book from 1 to 10 seats")
	@Schema(description = "List of seats to book")
	private List<SeatSelectionRequest> seats;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Seat selection with ticket type")
	public static class SeatSelectionRequest {

		@NotNull(message = "Seat ID is required")
		@Schema(description = "ID of the seat", example = "45")
		private Long seatId;

		@NotNull(message = "Ticket type ID is required")
		@Schema(description = "ID of the ticket type", example = "2")
		private Long ticketTypeId;
	}
}