package ua.lviv.bas.cinema.dto.booking.response;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.cinema.enums.SeatType;

public record SeatReservationResponse(@Schema(description = "Session ID", example = "1") Long sessionId,

		@Schema(description = "Movie title", example = "Inception") String movieTitle,

		@Schema(description = "Base price", example = "250.00") BigDecimal basePrice,

		@Schema(description = "Hall name", example = "Hall A") String hallName,

		@Schema(description = "Available seats count", example = "105") Integer availableSeats,

		@Schema(description = "List of seats") List<SeatInfo> seats) {
	public record SeatInfo(@Schema(description = "Seat ID", example = "45") Long id,

			@Schema(description = "Row number", example = "5") Integer row,

			@Schema(description = "Seat number", example = "12") Integer seatNumber,

			@Schema(description = "Seat type", example = "VIP") SeatType seatType,

			@Schema(description = "Is available", example = "true") Boolean available,

			@Schema(description = "Is temporarily reserved", example = "false") Boolean temporarilyReserved,

			@Schema(description = "Is seat active (technically available)", example = "true") Boolean active,

			@Schema(description = "Calculated prices for ticket types") List<TicketPriceInfo> ticketPrices) {
	}

	public record TicketPriceInfo(@Schema(description = "Ticket type ID", example = "1") Long ticketTypeId,

			@Schema(description = "Ticket type name", example = "Adult") String ticketTypeName,

			@Schema(description = "Final price", example = "250.00") BigDecimal finalPrice) {
	}
}