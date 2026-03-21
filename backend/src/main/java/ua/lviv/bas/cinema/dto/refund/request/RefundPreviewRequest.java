package ua.lviv.bas.cinema.dto.refund.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record RefundPreviewRequest(
		@NotNull(message = "Ticket ID is required") @Schema(description = "Ticket ID for refund", example = "123") Long ticketId) {
}