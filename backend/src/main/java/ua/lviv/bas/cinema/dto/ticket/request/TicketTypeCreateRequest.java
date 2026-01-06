package ua.lviv.bas.cinema.dto.ticket.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.TicketTypeCategory;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a new Ticket Type")
public class TicketTypeCreateRequest {

	@NotBlank(message = "Code must not be blank")
	@Size(min = 1, max = 20, message = "Code must be between 1 and 20 characters")
	@Schema(description = "Unique code for the ticket type", example = "ADULT", requiredMode = Schema.RequiredMode.REQUIRED)
	private String code;

	@NotBlank(message = "Display name must not be blank")
	@Size(min = 1, max = 50, message = "Display name must be between 1 and 50 characters")
	@Schema(description = "Display name for the ticket type", example = "Adult Ticket", requiredMode = Schema.RequiredMode.REQUIRED)
	private String displayName;

	@NotNull(message = "Price multiplier must not be null")
	@DecimalMin(value = "0.01", inclusive = false, message = "Price multiplier must be greater than 0")
	@DecimalMax(value = "9.99", inclusive = true, message = "Price multiplier must be less than or equal to 9.99")
	private BigDecimal priceMultiplier;

	@Min(value = 0, message = "Minimum age must be at least 0")
	@Max(value = 100, message = "Minimum age must be at most 100")
	@Schema(description = "Minimum age for this ticket type", example = "18")
	private Integer minAge;

	@Min(value = 0, message = "Maximum age must be at least 0")
	@Max(value = 100, message = "Maximum age must be at most 100")
	@Schema(description = "Maximum age for this ticket type", example = "65")
	private Integer maxAge;

	@Schema(description = "Indicates if a document is required for this ticket type", example = "false")
	@Builder.Default
	private boolean requiresDocument = false;

	@Size(max = 100, message = "Document type must be at most 100 characters")
	@Schema(description = "Type of document required for this ticket type", example = "ID Card", nullable = true)
	private String documentType;

	@Schema(description = "Indicates if the ticket type is active", example = "true")
	@Builder.Default
	private boolean active = true;

	@Schema(description = "Category of the ticket type", example = "STANDARD", requiredMode = Schema.RequiredMode.REQUIRED)
	private TicketTypeCategory category;
}