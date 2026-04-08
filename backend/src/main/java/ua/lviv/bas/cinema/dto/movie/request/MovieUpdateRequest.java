package ua.lviv.bas.cinema.dto.movie.request;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.validator.constraints.URL;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
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
import ua.lviv.bas.cinema.domain.cinema.enums.AgeRating;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating an existing movie")
public class MovieUpdateRequest {

	@Schema(description = "Title of the movie", example = "Inception 2", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 50)
	@NotBlank(message = "Title is required")
	@Size(max = 50, message = "Title must be less than 50 characters")
	private String title;

	@Schema(description = "URL to the movie trailer", example = "https://www.youtube.com/watch?v=YoHD9XEInc0", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Trailer URL is required")
	@URL(message = "Trailer must be a valid URL")
	private String trailerUrl;

	@Schema(description = "Movie description/synopsis", example = "A thief who steals corporate secrets through dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O.", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 1000)
	@NotBlank(message = "Description is required")
	@Size(max = 1000, message = "Description must be less than 1000 characters")
	private String description;

	@Schema(description = "Duration of the movie in minutes", example = "148", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1")
	@NotNull(message = "Duration is required")
	@Min(value = 1, message = "Duration must be at least 1 minute")
	private Integer durationMinutes;

	@Schema(description = "Release date of the movie", example = "2024-01-15", requiredMode = Schema.RequiredMode.REQUIRED, type = "string", format = "date")
	@NotNull(message = "Release date is required")
	@FutureOrPresent(message = "Release date must be today or in the future")
	private LocalDate releaseDate;

	@Schema(description = "Date when the movie stops showing in cinemas", example = "2024-03-15", requiredMode = Schema.RequiredMode.REQUIRED, type = "string", format = "date")
	@NotNull(message = "End showing date is required")
	@Future(message = "End showing date must be in the future")
	private LocalDate endShowingDate;

	@Schema(description = "Age rating of the movie", example = "PEGI_12", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {
			"PEGI_3", "PEGI_7", "PEGI_12", "PEGI_16", "PEGI_18" })
	@NotNull(message = "Age rating is required")
	private AgeRating ageRating;

	@Schema(description = "List of genre IDs for the movie", example = "[1, 2, 3]", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "At least one genre is required")
	@Size(min = 1, message = "At least one genre is required")
	private List<Long> genreIds;

	@Schema(description = "List of actor IDs for the movie cast", example = "[4, 5, 6]", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "At least one cast member is required")
	@Size(min = 1, message = "At least one cast member is required")
	private List<Long> actorIds;

	@Schema(description = "List of director IDs for the movie", example = "[7]", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "At least one director is required")
	@Size(min = 1, message = "At least one director is required")
	private List<Long> directorIds;

	@Schema(description = "List of screenwriter IDs for the movie", example = "[8]", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "At least one screenwriter is required")
	@Size(min = 1, message = "At least one screenwriter is required")
	private List<Long> screenwriterIds;

	@Schema(description = "Movie poster image file (JPG, PNG). Provide only if updating the poster.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	private MultipartFile posterFile;

	@Schema(description = "Flag to remove existing poster (set to true to remove poster)", example = "false", defaultValue = "false")
	@Builder.Default
	private Boolean removePoster = false;
}