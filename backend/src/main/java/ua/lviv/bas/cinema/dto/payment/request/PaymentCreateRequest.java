package ua.lviv.bas.cinema.dto.payment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to create a payment for a booking")
public record PaymentCreateRequest(
        @NotNull(message = "Booking ID is required")
        @Schema(description = "Booking ID to pay for", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
        Long bookingId
) {
}