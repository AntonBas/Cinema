package ua.lviv.bas.cinema.dto.hall.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CinemaHallListResponse(
        @Schema(description = "Unique identifier of the cinema hall", example = "1")
        Long id,

        @Schema(description = "Name of the cinema hall", example = "Hall A - Dolby Atmos")
        String name,

        @Schema(description = "Total capacity of the cinema hall (number of seats)", example = "150")
        int capacity
) {
}