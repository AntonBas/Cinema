package ua.lviv.bas.cinema.dto.movie;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.validator.constraints.URL;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.AgeRating;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieCreateRequest {

	@NotBlank(message = "Title is required")
	@Size(max = 255, message = "Title must be less than 255 characters")
	private String title;

	@NotBlank(message = "Trailer URL is required")
	@URL(message = "Trailer must be a valid URL")
	private String trailerUrl;

	@NotBlank(message = "Description is required")
	@Size(max = 1000, message = "Description must be less than 1000 characters")
	private String description;

	@NotNull(message = "Duration is required")
	@Min(value = 1, message = "Duration must be at least 1 minute")
	private Integer durationMinutes;

	@NotNull(message = "Release date is required")
	@FutureOrPresent(message = "Release date must be today or in the future")
	private LocalDate releaseDate;

	@NotNull(message = "End showing date is required")
	@Future(message = "End showing date must be in the future")
	private LocalDate endShowingDate;

	@NotNull(message = "Age rating is required")
	private AgeRating ageRating;

	@NotNull(message = "At least one genre is required")
	@Size(min = 1, message = "At least one genre is required")
	private List<Long> genreIds;

	@NotNull(message = "At least one cast member is required")
	@Size(min = 1, message = "At least one cast member is required")
	private List<Long> actorIds;

	@NotNull(message = "At least one director is required")
	@Size(min = 1, message = "At least one director is required")
	private List<Long> directorIds;

	@NotNull(message = "At least one screenwriter is required")
	@Size(min = 1, message = "At least one screenwriter is required")
	private List<Long> screenwriterIds;

	private MultipartFile posterFile;

	@AssertTrue(message = "End showing date must be after release date")
	public boolean isEndDateValid() {
		if (releaseDate == null || endShowingDate == null) {
			return true;
		}
		return endShowingDate.isAfter(releaseDate);
	}
}