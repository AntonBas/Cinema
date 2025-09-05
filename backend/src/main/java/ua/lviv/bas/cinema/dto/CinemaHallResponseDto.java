package ua.lviv.bas.cinema.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CinemaHallResponseDto {
	private Long id;
	private String name;
	private int capacity;
	private List<SeatDto> seats;
}
