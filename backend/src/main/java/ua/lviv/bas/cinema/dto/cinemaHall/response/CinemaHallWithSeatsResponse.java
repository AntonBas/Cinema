package ua.lviv.bas.cinema.dto.cinemaHall.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for cinema hall with detailed seat information")
public class CinemaHallWithSeatsResponse {

	@Schema(description = "Unique identifier of the cinema hall", example = "1")
	private Long id;

	@Schema(description = "Name of the cinema hall", example = "Hall A - Dolby Atmos")
	private String name;

	@Schema(description = "Total capacity of the cinema hall (number of seats)", example = "150")
	private int capacity;

	@Schema(description = "List of all seats in the cinema hall with their details", example = """
			[
			    {
			        "id": 1,
			        "rowNumber": 1,
			        "seatNumber": 1,
			        "seatType": "VIP",
			        "isAvailable": true
			    },
			    {
			        "id": 2,
			        "rowNumber": 1,
			        "seatNumber": 2,
			        "seatType": "VIP",
			        "isAvailable": true
			    }
			]
			""")
	private List<SeatResponse> seats;
}