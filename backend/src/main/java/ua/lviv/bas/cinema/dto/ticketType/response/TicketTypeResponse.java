package ua.lviv.bas.cinema.dto.ticketType.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.ticket.TicketTypeCategory;

public record TicketTypeResponse(@Schema(description = "Unique identifier of the ticket type", example = "1") Long id,

		@Schema(description = "Display name for the ticket type", example = "Adult Ticket") String displayName,

		@Schema(description = "Price multiplier for the ticket type", example = "1.00") BigDecimal priceMultiplier,

		@Schema(description = "Minimum age for this ticket type", example = "18") Integer minAge,

		@Schema(description = "Maximum age for this ticket type", example = "65") Integer maxAge,

		@Schema(description = "Indicates if a document is required for this ticket type", example = "false") boolean requiresDocument,

		@Schema(description = "Type of document required for this ticket type", example = "ID Card", nullable = true) String documentType,

		@Schema(description = "Indicates if the ticket type is active", example = "true") boolean active,

		@Schema(description = "Category of the ticket type", example = "STANDARD") TicketTypeCategory category) {
}