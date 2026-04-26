package ua.lviv.bas.cinema.dto.ticket.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Ticket information for cashier after QR code scan")
public record TicketCashierResponse(

        @Schema(description = "Ticket ID", example = "123")
        Long id,

        @Schema(description = "Unique ticket code from QR", example = "TKT-B1F0445950FE")
        String uniqueCode,

        @Schema(description = "Current ticket status", example = "ACTIVE")
        TicketStatus status,

        @Schema(description = "Movie title", example = "Dune: Part Two")
        String movieTitle,

        @Schema(description = "Session start time", example = "2026-04-17T20:00:00")
        LocalDateTime sessionTime,

        @Schema(description = "Hall name", example = "Hall №1")
        String hallName,

        @Schema(description = "Seat row", example = "4")
        String seatRow,

        @Schema(description = "Seat number", example = "8")
        Integer seatNumber,

        @Schema(description = "Ticket type name", example = "Student")
        String ticketType,

        @Schema(description = "Whether document check is required", example = "true")
        boolean requiresDocument,

        @Schema(description = "Type of document to verify", example = "Student ID", nullable = true)
        String documentType,

        @Schema(description = "Ticket holder email for verification", example = "basantonoleg@gmail.com")
        String userEmail,

        @Schema(description = "Final price paid", example = "500.00UAH")
        BigDecimal finalPrice
) {
}
