package ua.lviv.bas.cinema.dto.cinemaHall;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.dto.shared.SeatDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CinemaHallWithSeatsDto {

	private Long id;
	private String name;
	private int capacity;
	private List<SeatDto> seats;
}
