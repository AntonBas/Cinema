package ua.lviv.bas.cinema.booking.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@Schema(description = "Request to create a new booking")
public record BookingCreateRequest(
        @NotNull(message = "Session ID is required")
        @Schema(description = "Public session identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID sessionId,

        @Valid
        @Size(min = 1, max = 10, message = "Minimum 1, maximum 10 seats")
        @NotNull(message = "Seats list is required")
        @Schema(description = "List of selected seats", requiredMode = Schema.RequiredMode.REQUIRED)
        List<SeatSelectionRequest> seats,

        @Min(value = 0, message = "Bonus points cannot be negative")
        @Max(value = 100000, message = "Maximum 100000 bonus points allowed")
        @Schema(description = "Number of bonus points to use", example = "100", defaultValue = "0")
        Integer bonusPointsToUse
) {
    @Schema(description = "Seat selection details")
    public record SeatSelectionRequest(
            @NotNull(message = "Seat ID is required")
            @Schema(description = "Seat ID", example = "45", requiredMode = Schema.RequiredMode.REQUIRED)
            Long seatId,

            @NotNull(message = "Ticket type ID is required")
            @Schema(description = "Ticket type ID", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
            Long ticketTypeId
    ) {
    }
}