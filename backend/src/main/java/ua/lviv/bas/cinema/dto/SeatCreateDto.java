package ua.lviv.bas.cinema.dto;

import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;
import ua.lviv.bas.cinema.domain.enums.SeatType;

@Data
@Builder
public class SeatCreateDto {

	@Min(value = 1, message = "Row must be at least 1")
	private int row;

	@Min(value = 1, message = "Seat number must be at least 1")
	private int number;

	private SeatType seatType;
}
