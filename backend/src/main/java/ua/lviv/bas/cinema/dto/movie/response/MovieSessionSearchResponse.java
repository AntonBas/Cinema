package ua.lviv.bas.cinema.dto.movie.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieSessionSearchResponse {

	private Long id;
	private String title;
	private Integer releaseYear;
	private Integer durationMinutes;
}
