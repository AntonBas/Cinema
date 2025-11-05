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
public class SeatRowResponse {

	private Integer rowNumber;
	private int seatsCount;
	private List<SeatResponse> seats;
}