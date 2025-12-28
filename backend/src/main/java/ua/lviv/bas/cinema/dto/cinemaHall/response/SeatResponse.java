package ua.lviv.bas.cinema.dto.cinemaHall.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.SeatType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for individual seat information")
public class SeatResponse {

	@Schema(description = "Unique identifier of the seat", example = "1")
	private Long id;

	@Schema(description = "Row number where the seat is located (starting from 1)", example = "5")
	private Integer row;

	@Schema(description = "Seat number within the row (starting from 1)", example = "12")
	private Integer number;

	@Schema(description = "Type of the seat", example = "VIP", allowableValues = { "STANDARD", "VIP", "COUPLE",
			"DISABLED" })
	private SeatType seatType;

	@Schema(description = "Whether the seat is active (not broken/disabled)", example = "true")
	private boolean active;
}