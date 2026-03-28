package ua.lviv.bas.cinema.dto.ticketType.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;

public record TicketResponse(@Schema(description = "Ticket ID", example = "999") Long id,

		@Schema(description = "Ticket code", example = "TKT-20240115-ABC123") String ticketCode,

		@Schema(description = "QR code URL", example = "/api/tickets/999/qr") String qrCodeUrl,

		@Schema(description = "Ticket status", example = "ACTIVE") TicketStatus status,

		@Schema(description = "Purchase time", example = "2024-01-15T14:32:00") LocalDateTime purchaseTime,

		@Schema(description = "Final price", example = "225.00") BigDecimal price,

		@Schema(description = "Ticket type", example = "Adult") String ticketType,

		@Schema(description = "Movie title", example = "Inception") String movieTitle,

		@Schema(description = "Session time", example = "2024-01-15T18:30:00") LocalDateTime sessionTime,

		@Schema(description = "Hall name", example = "Hall A") String hallName,

		@Schema(description = "Row number", example = "5") Integer row,

		@Schema(description = "Seat number", example = "12") Integer seatNumber) {
}