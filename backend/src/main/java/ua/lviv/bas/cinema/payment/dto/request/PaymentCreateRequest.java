package ua.lviv.bas.cinema.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request to create a payment for a booking")
public record PaymentCreateRequest(
        @NotNull(message = "Booking ID is required")
        @Schema(description = "Public booking identifier to pay for", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID bookingId
) {
}