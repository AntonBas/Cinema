package ua.lviv.bas.cinema.dto.payment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.PaymentMethod;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a payment for booking")
public class PaymentCreateRequest {

	@NotNull(message = "Booking ID is required")
	@Schema(description = "ID of the booking to pay for", example = "123")
	private Long bookingId;

	@NotNull(message = "Payment method is required")
	@Schema(description = "Payment method", example = "CARD")
	private PaymentMethod paymentMethod;

	@Builder.Default
	@Min(value = 0, message = "Bonus points cannot be negative")
	@Max(value = 500, message = "Maximum 500 bonus points allowed")
	@Schema(description = "Bonus points to use", example = "50", defaultValue = "0")
	private Integer bonusPointsToUse = 0;
}