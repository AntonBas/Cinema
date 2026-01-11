package ua.lviv.bas.cinema.dto.refund.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for refund preview")
public class RefundPreviewRequest {

	@NotNull(message = "Ticket ID is required")
	@Schema(description = "Ticket ID for refund", example = "123")
	private Long ticketId;
}