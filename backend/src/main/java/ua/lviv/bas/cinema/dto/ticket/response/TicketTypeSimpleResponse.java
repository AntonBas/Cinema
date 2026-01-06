package ua.lviv.bas.cinema.dto.ticket.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Simple Response DTO for Ticket Type")
public class TicketTypeSimpleResponse {

	@Schema(description = "Unique identifier of the ticket type", example = "1")
	private Long id;

	@Schema(description = "Unique code for the ticket type", example = "ADULT")
	private String code;

	@Schema(description = "Display name for the ticket type", example = "Adult Ticket")
	private String displayName;

	@Schema(description = "Price multiplier for the ticket type", example = "1.00")
	private BigDecimal priceMultiplier;

	@Schema(description = "Indicates if the ticket type is active", example = "true")
	private boolean active;
}