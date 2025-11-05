package ua.lviv.bas.cinema.dto.session.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieShortResponse;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {

	private Long id;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private BigDecimal price;
	private MovieShortResponse movie;
	private CinemaHallResponse hall;
	private boolean available;
}
