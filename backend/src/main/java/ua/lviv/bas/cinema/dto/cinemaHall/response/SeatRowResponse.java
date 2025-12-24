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
@Schema(description = "Response DTO for a row of seats in a cinema hall")
public class SeatRowResponse {

	@Schema(description = "Row number (starting from 1)", example = "1")
	private Integer rowNumber;

	@Schema(description = "Total number of seats in this row", example = "15")
	private int seatsCount;

	@Schema(description = "List of seats in this row", example = """
			[
			    {
			        "id": 1,
			        "row": 1,
			        "number": 1,
			        "seatType": "VIP"
			    },
			    {
			        "id": 2,
			        "row": 1,
			        "number": 2,
			        "seatType": "VIP"
			    }
			]
			""")
	private List<SeatResponse> seats;
}