package ua.lviv.bas.cinema.dto.hall.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

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
}