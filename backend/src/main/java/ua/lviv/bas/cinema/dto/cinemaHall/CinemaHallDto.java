package ua.lviv.bas.cinema.dto.cinemaHall;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CinemaHallDto {
	private Long id;
	private String name;
	private int capacity;
}
