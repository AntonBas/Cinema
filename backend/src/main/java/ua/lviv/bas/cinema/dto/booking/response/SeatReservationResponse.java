package ua.lviv.bas.cinema.dto.booking.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.cinema.enums.SeatType;

import java.math.BigDecimal;
import java.util.List;

public record SeatReservationResponse(
        @Schema(description = "Session ID", example = "1")
        Long sessionId,

        @Schema(description = "Movie title", example = "Inception")
        String movieTitle,

        @Schema(description = "Base price", example = "250.00")
        BigDecimal basePrice,

        @Schema(description = "Hall name", example = "Hall A")
        String hallName,

        @Schema(description = "Available seats count", example = "105")
        Integer availableSeats,

        @Schema(description = "List of seats")
        List<SeatInfo> seats
) {
    public record SeatInfo(
            @Schema(description = "Seat ID", example = "45")
            Long id,

            @Schema(description = "Row number", example = "5")
            Integer row,

            @Schema(description = "Seat number", example = "12")
            Integer seatNumber,

            @Schema(description = "Seat type", example = "VIP")
            SeatType seatType,

            @Schema(description = "Is available", example = "true")
            boolean available,

            @Schema(description = "Is temporarily reserved", example = "false")
            boolean temporarilyReserved,

            @Schema(description = "Is seat active", example = "true")
            boolean active,

            @Schema(description = "Calculated prices for ticket types")
            List<TicketPriceInfo> ticketPrices
    ) {
    }

    public record TicketPriceInfo(
            @Schema(description = "Ticket type ID", example = "1")
            Long ticketTypeId,

            @Schema(description = "Ticket type name", example = "Adult")
            String ticketTypeName,

            @Schema(description = "Final price", example = "250.00")
            BigDecimal finalPrice,

            @Schema(description = "Minimum age", example = "12")
            Integer minAge,

            @Schema(description = "Maximum age", example = "25")
            Integer maxAge,

            @Schema(description = "Whether document is required", example = "true")
            boolean requiresDocument,

            @Schema(description = "Type of document required", example = "Student ID")
            String documentType
    ) {
    }
}