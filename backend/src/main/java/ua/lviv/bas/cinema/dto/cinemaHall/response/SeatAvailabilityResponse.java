package ua.lviv.bas.cinema.dto.cinemaHall.response;

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
@Schema(description = "Seat availability for a session")
public class SeatAvailabilityResponse {

	@Schema(description = "Session information")
	private SessionInfo session;

	@Schema(description = "Available seats with prices")
	private List<SeatInfo> seats;

	@Schema(description = "Available ticket types")
	private List<TicketTypeInfo> ticketTypes;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Session information")
	public static class SessionInfo {
		@Schema(description = "Session ID", example = "1")
		private Long id;

		@Schema(description = "Movie title", example = "Inception")
		private String movieTitle;

		@Schema(description = "Base price", example = "150.00")
		private BigDecimal basePrice;

		@Schema(description = "Hall name", example = "Hall A - Dolby Atmos")
		private String hallName;

		@Schema(description = "Total seats", example = "150")
		private Integer totalSeats;

		@Schema(description = "Available seats", example = "105")
		private Integer availableSeats;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Seat information with availability")
	public static class SeatInfo {
		@Schema(description = "Seat ID", example = "45")
		private Long id;

		@Schema(description = "Row number", example = "5")
		private Integer row;

		@Schema(description = "Seat number", example = "12")
		private Integer seatNumber;

		@Schema(description = "Seat type", example = "VIP")
		private SeatType seatType;

		@Schema(description = "Is seat available", example = "true")
		private Boolean available;

		@Schema(description = "Is seat active", example = "true")
		private Boolean active;

		@Schema(description = "Price for this seat", example = "225.00")
		private BigDecimal price;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Available ticket type")
	public static class TicketTypeInfo {
		@Schema(description = "Ticket type ID", example = "2")
		private Long id;

		@Schema(description = "Display name", example = "Adult")
		private String name;

		@Schema(description = "Price multiplier", example = "1.5")
		private BigDecimal multiplier;

		@Schema(description = "Description", example = "Standard adult ticket")
		private String description;
	}
}