package ua.lviv.bas.cinema.dto.movie.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieShortResponse {

	private Long id;
	private String title;
	private Integer durationMinutes;
	private String posterFileName;
}
