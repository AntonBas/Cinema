package ua.lviv.bas.cinema.dto.booking.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.booking.status.ReservationStatus;

@Schema(description = "Seat status response")
public record SeatStatusResponse(
        @Schema(description = "Seat ID", example = "45")
        Long seatId,

        @Schema(description = "Reservation status", example = "PENDING")
        ReservationStatus status
) {
}