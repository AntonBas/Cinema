package ua.lviv.bas.cinema.dto.shared;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatRowDto {
	private Integer rowNumber;
	private int seatsCount;
	private List<SeatDto> seats;
}