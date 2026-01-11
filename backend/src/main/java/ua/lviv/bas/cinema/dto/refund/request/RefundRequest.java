package ua.lviv.bas.cinema.dto.refund.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to process ticket refund")
public class RefundRequest {

	@NotNull(message = "Ticket ID is required")
	@Schema(description = "Ticket ID for refund", example = "123")
	private Long ticketId;

	@Size(max = 500, message = "Reason must not exceed 500 characters")
	@Schema(description = "Refund reason", example = "Cannot attend the session")
	private String reason;
}