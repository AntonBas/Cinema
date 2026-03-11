package ua.lviv.bas.cinema.dto.ticket.request;

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
@Schema(description = "Request DTO for filtering tickets")
public class TicketFilterRequest {

	@Schema(description = "Ticket status", example = "ACTIVE")
	private TicketStatus status;

	@Schema(description = "Movie title search", example = "Inception")
	private String movieTitle;
}