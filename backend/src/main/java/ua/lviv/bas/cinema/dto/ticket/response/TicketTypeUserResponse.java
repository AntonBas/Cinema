package ua.lviv.bas.cinema.dto.ticket.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

public record TicketTypeUserResponse(@Schema(description = "Ticket type ID", example = "1") Long id,

		@Schema(description = "Display name", example = "Student Ticket") String displayName,

		@Schema(description = "Price multiplier", example = "0.70") BigDecimal priceMultiplier,

		@Schema(description = "Whether document is required", example = "true") boolean requiresDocument,

		@Schema(description = "Type of document required", example = "Student ID") String documentType) {
}