package ua.lviv.bas.cinema.dto.cinemaHall.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record SeatRowResponse(@Schema(description = "Row number (starting from 1)", example = "1") Integer rowNumber,

		@Schema(description = "Total number of seats in this row", example = "15") int seatsCount,

		@Schema(description = "List of seats in this row") List<SeatResponse> seats) {
}