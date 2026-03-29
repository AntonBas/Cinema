package ua.lviv.bas.cinema.dto.ticket.request;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;

public record TicketFilterRequest(@Schema(description = "Ticket status", example = "ACTIVE") TicketStatus status,

		@Schema(description = "Movie title search", example = "Inception") String movieTitle) {
}