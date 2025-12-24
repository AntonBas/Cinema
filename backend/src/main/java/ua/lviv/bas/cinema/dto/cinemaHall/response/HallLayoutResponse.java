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
@Schema(description = "Response DTO for detailed cinema hall layout with seat organization")
public class HallLayoutResponse {

	@Schema(description = "Unique identifier of the cinema hall", example = "1")
	private Long hallId;

	@Schema(description = "Name of the cinema hall", example = "Hall A - Dolby Atmos")
	private String hallName;

	@Schema(description = "Total number of rows in the hall", example = "10")
	private int totalRows;

	@Schema(description = "Maximum number of seats in any row", example = "15")
	private int maxSeatsPerRow;

	@Schema(description = "Total number of seats in the hall", example = "150")
	private int totalSeats;

	@Schema(description = "List of rows with their seat information", example = """
			[
			    {
			        "rowNumber": 1,
			        "seats": [
			            {
			                "id": 1,
			                "seatNumber": 1,
			                "seatType": "VIP",
			                "isAvailable": true
			            },
			            {
			                "id": 2,
			                "seatNumber": 2,
			                "seatType": "VIP",
			                "isAvailable": true
			            }
			        ]
			    }
			]
			""")
	private List<SeatRowResponse> rows;
}