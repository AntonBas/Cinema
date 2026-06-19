package ua.lviv.bas.cinema.dto.hall.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Cinema hall layout with seat grid")
public record HallLayoutResponse(
        @Schema(description = "Unique identifier of the cinema hall", example = "1")
        Long hallId,

        @Schema(description = "Name of the cinema hall", example = "Hall A - Dolby Atmos")
        String hallName,

        @Schema(description = "Total number of rows in the hall", example = "10")
        int totalRows,

        @Schema(description = "Maximum number of seats in any row", example = "15")
        int maxSeatsPerRow,

        @Schema(description = "Total number of seats in the hall", example = "150")
        int totalSeats,

        @Schema(description = "List of rows with their seat information")
        List<SeatRowResponse> rows
) {
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
}