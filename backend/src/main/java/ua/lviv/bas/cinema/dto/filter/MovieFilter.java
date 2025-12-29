package ua.lviv.bas.cinema.dto.filter;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Schema(description = "Filter DTO for searching and filtering movies")
public class MovieFilter {

	@Schema(description = "Search term for movie title or description", example = "inception")
	private String searchTerm;

	@Schema(description = "Filter by movie status", example = "CURRENT")
	private MovieStatus status;

	@Schema(description = "Filter by age rating", example = "PG_13")
	private AgeRating ageRating;

	@Schema(description = "Filter by minimum duration in minutes", example = "60", minimum = "1")
	@Min(value = 1, message = "Min duration must be at least 1 minute")
	private Integer minDuration;

	@Schema(description = "Filter by maximum duration in minutes", example = "180", minimum = "1")
	@Min(value = 1, message = "Max duration must be at least 1 minute")
	private Integer maxDuration;

	@Schema(description = "Filter by release date from", example = "2024-01-01", type = "string", format = "date")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate releaseDateFrom;

	@Schema(description = "Filter by release date to", example = "2024-12-31", type = "string", format = "date")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate releaseDateTo;

	@Schema(description = "Whether to include archived movies (admin view)", example = "false", defaultValue = "false")
	@Builder.Default
	private boolean adminView = false;

	@Schema(description = "Page number for pagination (0-based)", example = "0", defaultValue = "0", minimum = "0")
	@Builder.Default
	@Min(value = 0, message = "Page must be greater than or equal to 0")
	private int page = 0;

	@Schema(description = "Number of items per page", example = "20", defaultValue = "20", minimum = "1", maximum = "100")
	@Builder.Default
	@Min(value = 1, message = "Size must be greater than or equal to 1")
	@Max(value = 100, message = "Size must be less than or equal to 100")
	private int size = 20;

	@Schema(description = "Field to sort by", example = "title", defaultValue = "title", allowableValues = { "title",
			"releaseDate", "durationMinutes", "ageRating", "status" })
	@Builder.Default
	private String sortBy = "title";

	@Schema(description = "Sort direction", example = "ASC", defaultValue = "ASC", allowableValues = { "ASC", "DESC" })
	@Builder.Default
	private SortDirection sortDirection = SortDirection.ASC;

	@Schema(description = "Sort direction enumeration")
	public enum SortDirection {
		@Schema(description = "Ascending order")
		ASC, @Schema(description = "Descending order")
		DESC
	}
}