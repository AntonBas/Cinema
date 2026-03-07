package ua.lviv.bas.cinema.dto.booking.response;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.SeatType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Seat availability for session")
public class SeatReservationResponse {

	@Schema(description = "Session ID", example = "1")
	private Long sessionId;

	@Schema(description = "Movie title", example = "Inception")
	private String movieTitle;

	@Schema(description = "Base price", example = "250.00")
	private BigDecimal basePrice;

	@Schema(description = "Hall name", example = "Hall A")
	private String hallName;

	@Schema(description = "Available seats count", example = "105")
	private Integer availableSeats;

	@Schema(description = "List of seats")
	private List<SeatInfo> seats;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Seat information")
	public static class SeatInfo {

		@Schema(description = "Seat ID", example = "45")
		private Long id;

		@Schema(description = "Row number", example = "5")
		private Integer row;

		@Schema(description = "Seat number", example = "12")
		private Integer seatNumber;

		@Schema(description = "Seat type", example = "VIP")
		private SeatType seatType;

		@Schema(description = "Is available", example = "true")
		private Boolean available;

		@Schema(description = "Is temporarily reserved", example = "false")
		private Boolean temporarilyReserved;

		@Schema(description = "Is seat active (technically available)", example = "true")
		private Boolean active;

		@Schema(description = "Calculated prices for ticket types")
		private List<TicketPriceInfo> ticketPrices;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Ticket price information")
	public static class TicketPriceInfo {

		@Schema(description = "Ticket type ID", example = "1")
		private Long ticketTypeId;

		@Schema(description = "Ticket type name", example = "Adult")
		private String ticketTypeName;

		@Schema(description = "Final price", example = "250.00")
		private BigDecimal finalPrice;
	}
}