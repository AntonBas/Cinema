package ua.lviv.bas.cinema.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ua.lviv.bas.cinema.domain.enums.SeatType;

@Data
@Builder
public class CinemaHallCreateDto {

	private Long id;

	@NotBlank(message = "Hall name is required")
	@Size(max = 30, message = "Maximum 30 characters")
	private String name;

	@Min(value = 1, message = "Minimum 1 row")
	@Max(value = 20, message = "Maximum 20 rows")
	private int rows;

	@Min(value = 1, message = "Minimum 1 seat in a row")
	@Max(value = 30, message = "Maximum 30 seat in a row")
	private int seatsPerRow;

	@Builder.Default
	private SeatType defaultSeatType = SeatType.STANDARD;
}
