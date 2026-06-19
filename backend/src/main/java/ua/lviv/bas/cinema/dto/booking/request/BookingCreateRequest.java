package ua.lviv.bas.cinema.dto.booking.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Request to create a new booking")
public record BookingCreateRequest(
        @NotNull(message = "Session ID is required")
        @Schema(description = "Session ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long sessionId,

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