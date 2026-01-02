package ua.lviv.bas.cinema.dto.ticket.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ticket information")
public class TicketResponse {

	@Schema(description = "Ticket ID", example = "999")
	private Long id;

	@Schema(description = "Unique ticket code", example = "TKT-20240115-ABC123")
	private String ticketCode;

	@Schema(description = "QR code URL", example = "/api/tickets/999/qr")
	private String qrCodeUrl;

	@Schema(description = "Ticket status", example = "ACTIVE")
	private TicketStatus status;

	@Schema(description = "Purchase time", example = "2024-01-15T14:32:00")
	private LocalDateTime purchaseTime;

	@Schema(description = "Final price", example = "225.00")
	private BigDecimal price;

	@Schema(description = "Ticket type", example = "Adult")
	private String ticketType;

	@Schema(description = "Movie title", example = "Inception")
	private String movieTitle;

	@Schema(description = "Session start time", example = "2024-01-15T18:30:00")
	private LocalDateTime sessionTime;

	@Schema(description = "Hall name", example = "Hall A - Dolby Atmos")
	private String hallName;

	@Schema(description = "Row number", example = "5")
	private Integer row;

	@Schema(description = "Seat number", example = "12")
	private Integer seatNumber;

	@Schema(description = "User full name", example = "John Doe")
	private String userName;
}