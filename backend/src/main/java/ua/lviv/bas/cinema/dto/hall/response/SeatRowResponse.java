package ua.lviv.bas.cinema.dto.hall.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Row layout information with seats")
public record SeatRowResponse(
        @Schema(description = "Row number (starting from 1)", example = "1")
        Integer rowNumber,

        @Schema(description = "Total number of seats in this row", example = "15")
        int seatsCount,

        @Schema(description = "List of seats in this row")
        List<SeatResponse> seats
) {
}