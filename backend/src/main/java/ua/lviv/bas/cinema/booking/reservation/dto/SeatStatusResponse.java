package ua.lviv.bas.cinema.booking.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.booking.reservation.ReservationStatus;

@Schema(description = "Seat status response")
public record SeatStatusResponse(
        @Schema(description = "Seat ID", example = "45")
        Long seatId,

        @Schema(description = "Reservation status", example = "PENDING")
        ReservationStatus status
) {
}