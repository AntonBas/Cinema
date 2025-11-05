package ua.lviv.bas.cinema.dto.cinemaHall;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.dto.shared.SeatRowDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HallLayoutDto {
	private Long hallId;
	private String hallName;
	private int totalRows;
	private int maxSeatsPerRow;
	private int totalSeats;
	private List<SeatRowDto> rows;
}