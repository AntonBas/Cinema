package ua.lviv.bas.cinema.dto.cinemaHall.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CinemaHallWithSeatsResponse {

	private Long id;
	private String name;
	private int capacity;
	private List<SeatResponse> seats;
}
