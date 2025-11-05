package ua.lviv.bas.cinema.dto.shared;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.SeatType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatDto {
	private Long id;
	private int row;
	private int number;
	private SeatType seatType;

}