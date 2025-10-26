package ua.lviv.bas.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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