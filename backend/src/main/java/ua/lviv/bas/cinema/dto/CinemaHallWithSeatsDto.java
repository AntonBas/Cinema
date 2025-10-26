package ua.lviv.bas.cinema.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
