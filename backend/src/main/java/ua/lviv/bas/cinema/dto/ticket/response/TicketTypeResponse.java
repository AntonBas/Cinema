package ua.lviv.bas.cinema.dto.ticket.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.TicketTypeCategory;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for Ticket Type")
public class TicketTypeResponse {

	@Schema(description = "Unique identifier of the ticket type", example = "1")
	private Long id;

	@Schema(description = "Unique code for the ticket type", example = "ADULT")
	private String code;

	@Schema(description = "Display name for the ticket type", example = "Adult Ticket")
	private String displayName;

	@Schema(description = "Price multiplier for the ticket type", example = "1.00")
	private BigDecimal priceMultiplier;

	@Schema(description = "Minimum age for this ticket type", example = "18")
	private Integer minAge;

	@Schema(description = "Maximum age for this ticket type", example = "65")
	private Integer maxAge;

	@Schema(description = "Indicates if a document is required for this ticket type", example = "false")
	private boolean requiresDocument;

	@Schema(description = "Type of document required for this ticket type", example = "ID Card", nullable = true)
	private String documentType;

	@Schema(description = "Indicates if the ticket type is active", example = "true")
	private boolean active;

	@Schema(description = "Category of the ticket type", example = "STANDARD")
	private TicketTypeCategory category;

	@Schema(description = "Timestamp when the ticket type was created", example = "2024-01-01T12:00:00")
	private LocalDateTime createdAt;

	@Schema(description = "Timestamp when the ticket type was last updated", example = "2024-01-02T15:30:00")
	private LocalDateTime updatedAt;
}