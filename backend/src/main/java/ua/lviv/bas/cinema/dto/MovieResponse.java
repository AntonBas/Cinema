package ua.lviv.bas.cinema.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse {

	private Long id;
	private String title;
	private String slug;
	private String posterUrl;
	private Integer durationMinutes;
	private AgeRating ageRating;
	private LocalDate releaseDate;
	private MovieStatus status;

	private boolean currentlyShowing;
}