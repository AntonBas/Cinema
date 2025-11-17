package ua.lviv.bas.cinema.dto.cinemaHall.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.SeatType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CinemaHallRequest {

	@NotBlank(message = "Hall name is required")
	@Size(min = 2, max = 25, message = "Name must be between 2-25 characters")
	private String name;

	@NotNull(message = "Number of rows is required")
	@Min(value = 1, message = "Minimum 1 row")
	@Max(value = 20, message = "Maximum 20 rows")
	private Integer rows;

	@NotNull(message = "Seats per row is required")
	@Min(value = 1, message = "Minimum 1 seat in a row")
	@Max(value = 20, message = "Maximum 20 seats in a rows")
	private Integer seatsPerRow;

	@Builder.Default
	private SeatType defaultSeatType = SeatType.STANDARD;
}
