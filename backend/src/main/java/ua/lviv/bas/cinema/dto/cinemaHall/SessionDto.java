package ua.lviv.bas.cinema.dto.cinemaHall;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.dto.movie.MovieSimpleDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDto {

	private Long id;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private BigDecimal price;
	private MovieSimpleDto movie;
	private CinemaHallDto hall;
	private boolean available;
}
