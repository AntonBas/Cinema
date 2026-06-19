package ua.lviv.bas.cinema.dto.hall.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.cinema.enums.SeatType;

import java.util.List;

@Schema(description = "Cinema hall details")
public record CinemaHallResponse(
        @Schema(description = "Unique identifier of the cinema hall", example = "1")
        Long id,

        @Schema(description = "Name of the cinema hall", example = "Hall A - Dolby Atmos")
        String name,

        @Schema(description = "Number of rows in the cinema hall", example = "10")
        Integer rows,

        @Schema(description = "Number of seats per row", example = "15")
        Integer seatsPerRow,

        @Schema(description = "Default seat type for the hall", example = "STANDARD")
        SeatType defaultSeatType,

        @Schema(description = "List of rows that have COUPLE seats", example = "[4, 7]")
        List<Integer> coupleRows,

        @Schema(description = "Total capacity of the cinema hall (number of seats)", example = "150")
        Integer capacity
) {
}