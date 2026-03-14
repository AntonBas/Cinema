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
@Schema(description = "Response for user dropdown with document info")
public class TicketTypeUserResponse {

	@Schema(description = "Ticket type ID", example = "1")
	private Long id;

	@Schema(description = "Display name", example = "Student Ticket")
	private String displayName;

	@Schema(description = "Price multiplier", example = "0.70")
	private BigDecimal priceMultiplier;

	@Schema(description = "Whether document is required", example = "true")
	private boolean requiresDocument;

	@Schema(description = "Type of document required", example = "Student ID")
	private String documentType;
}