package ua.lviv.bas.cinema.dto.payment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request to create payment")
public class PaymentCreateRequest {

	@NotNull(message = "Booking ID is required")
	@Schema(description = "Booking ID to pay for", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
	private Long bookingId;
}