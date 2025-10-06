package ua.lviv.bas.cinema.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class MovieDto {

	private Long id;

	@NotBlank(message = "Movie title is required")
	@Size(max = 255, message = "Title must be less than 255 characters")
	private String title;

	@NotBlank(message = "Slug is required")
	@Pattern(regexp = "^[a-z0-9-]+$", message = "Slug can only contain lowercase letters, numbers and hyphens")
	private String slug;

	@NotBlank(message = "Trailer URL is required")
	@org.hibernate.validator.constraints.URL(message = "Trailer must be a valid URL")
	private String trailerUrl;

	@NotBlank(message = "Description is required")
	@Size(max = 1000, message = "Description must be less than 1000 characters")
	private String description;

	@NotNull(message = "Duration is required")
	@Min(value = 1, message = "Duration must be at least 1 minute")
	private Integer durationMinutes;

	@NotNull(message = "Release date is required")
	@FutureOrPresent(message = "Release date cannot be in the past")
	private LocalDate releaseDate;

	@NotNull(message = "End showing date is required")
	@Future(message = "End showing date must be in the future")
	private LocalDate endShowingDate;

	@NotNull(message = "Movie status is required")
	private MovieStatus status;

	private String posterFileName;

	@NotNull(message = "Age rating is required")
	private AgeRating ageRating;

	@NotNull(message = "Cast is required")
	@Size(min = 1, message = "At least one cast member is required")
	private List<Long> castIds;

	@NotNull(message = "Directors are required")
	@Size(min = 1, message = "At least one director is required")
	private List<Long> directorIds;

	@NotNull(message = "Screenwriters are required")
	@Size(min = 1, message = "At least one screenwriter is required")
	private List<Long> screenwriterIds;

	@NotNull(message = "Genres are required")
	@Size(min = 1, message = "At least one genre is required")
	private List<Long> genreIds;

	private MultipartFile posterFile;

	@AssertTrue(message = "End showing date must be after release date")
	public boolean isEndDateValid() {
		if (releaseDate == null || endShowingDate == null)
			return true;
		return endShowingDate.isAfter(releaseDate);
	}
}