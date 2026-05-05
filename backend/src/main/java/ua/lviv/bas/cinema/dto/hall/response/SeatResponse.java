package ua.lviv.bas.cinema.dto.hall.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.cinema.enums.SeatType;

@Schema(description = "Individual seat details")
public record SeatResponse(
        @Schema(description = "Unique identifier of the seat", example = "1")
        Long id,

        @Schema(description = "Row number where the seat is located (starting from 1)", example = "5")
        Integer row,

        @Schema(description = "Seat number within the row", example = "12")
        Integer number,

        @Schema(description = "Type of the seat", example = "VIP")
        SeatType seatType,

        @Schema(description = "Whether the seat is active (not broken/disabled)", example = "true")
        boolean active
) {
    @Schema(description = "Display name with seat count for COUPLE seats", example = "Couple (2 seats)")
    public String getDisplayName() {
        return seatType.getSeatsCount() > 1
                ? seatType.getDisplayName() + " (" + seatType.getSeatsCount() + " seats)"
                : seatType.getDisplayName();
    }
}