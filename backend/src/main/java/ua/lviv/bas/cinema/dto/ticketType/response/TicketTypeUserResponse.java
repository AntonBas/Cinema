package ua.lviv.bas.cinema.dto.ticketType.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record TicketTypeUserResponse(@Schema(description = "Ticket type ID", example = "1") Long id,

		@Schema(description = "Display name", example = "Student Ticket") String displayName) {
}