package ua.lviv.bas.cinema.dto.ticketType.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import ua.lviv.bas.cinema.domain.ticket.TicketTypeCategory;

import java.math.BigDecimal;

public record TicketTypeRequest(
        @NotBlank(message = "Display name must not be blank")
        @Size(min = 1, max = 50, message = "Display name must be between 1 and 50 characters")
        @Schema(description = "Display name for the ticket type", example = "Adult Ticket")
        String displayName,

        @NotNull(message = "Price multiplier must not be null")
        @DecimalMin(value = "0.01", inclusive = false, message = "Price multiplier must be greater than 0")
        @DecimalMax(value = "9.99", inclusive = true, message = "Price multiplier must be less than or equal to 9.99")
        @Schema(description = "Price multiplier", example = "1.0")
        BigDecimal priceMultiplier,

        @Min(value = 0, message = "Minimum age must be at least 0")
        @Max(value = 100, message = "Minimum age must be at most 100")
        @Schema(description = "Minimum age for this ticket type", example = "18")
        Integer minAge,

        @Min(value = 0, message = "Maximum age must be at least 0")
        @Max(value = 100, message = "Maximum age must be at most 100")
        @Schema(description = "Maximum age for this ticket type", example = "65")
        Integer maxAge,

        @Schema(description = "Indicates if a document is required for this ticket type", example = "false")
        boolean requiresDocument,

        @Size(max = 100, message = "Document type must be at most 100 characters")
        @Schema(description = "Type of document required for this ticket type", example = "ID Card", nullable = true)
        String documentType,

        @Schema(description = "Indicates if the ticket type is active", example = "true")
        boolean active,

        @NotNull(message = "Category must not be null")
        @Schema(description = "Category of the ticket type", example = "STANDARD", requiredMode = Schema.RequiredMode.REQUIRED)
        TicketTypeCategory category
) {
}