package ua.lviv.bas.cinema.dto.session.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionScheduleResponse {

	private Long id;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private BigDecimal price;
	private Integer availableSeats;

	private Long movieId;
	private String movieTitle;
	private String moviePosterFileName;
	private String movieAgeRating;
	private Integer movieDuration;

	private Long hallId;
	private String hallName;
	private String hallCapacity;

}
