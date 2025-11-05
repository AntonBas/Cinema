package ua.lviv.bas.cinema.dto.movie;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieDto {

	private Long id;
	private String title;
	private String slug;
	private String trailerUrl;
	private String description;
	private Integer durationMinutes;
	private LocalDate releaseDate;
	private LocalDate endShowingDate;
	private AgeRating ageRating;
	private MovieStatus status;
	private String posterFileName;
	private String posterUrl;

	private boolean currentlyShowing;
	private boolean upcoming;
	private boolean archived;
	private boolean active;

	private List<Long> genreIds;
	private List<Long> actorIds;
	private List<Long> directorIds;
	private List<Long> screenwriterIds;
}