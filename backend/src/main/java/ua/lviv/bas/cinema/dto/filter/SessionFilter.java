package ua.lviv.bas.cinema.dto.filter;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filter DTO for searching and filtering movie sessions")
public class SessionFilter {

	@Schema(description = "Filter sessions starting from this date and time (inclusive)", example = "2024-01-15T10:00:00", type = "string", format = "date-time")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime startTime;

	@Schema(description = "Filter sessions ending before this date and time (inclusive)", example = "2024-01-15T23:59:59", type = "string", format = "date-time")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime endTime;

	@Schema(description = "Filter sessions by cinema hall ID", example = "1")
	private Long hallId;

	@Schema(description = "Filter sessions by movie ID", example = "5")
	private Long movieId;

	@Schema(description = "Filter sessions by status", example = "SCHEDULED", allowableValues = { "SCHEDULED",
			"CANCELLED", "COMPLETED" })
	private CinemaSessionStatus status;

	@Schema(description = "Whether to include all sessions (admin view) or only available for users", example = "false", defaultValue = "false")
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

	@Schema(description = "Field to sort by", example = "startTime", defaultValue = "startTime", allowableValues = {
			"startTime", "endTime", "hallId", "movieId", "status" })
	@Builder.Default
	private String sortBy = "startTime";

	@Schema(description = "Sort direction", example = "ASC", defaultValue = "ASC", allowableValues = { "ASC", "DESC" })
	@Builder.Default
	private SortDirection sortDirection = SortDirection.ASC;

	@Schema(description = "Sort direction enumeration")
	public enum SortDirection {
		@Schema(description = "Ascending order")
		ASC,

		@Schema(description = "Descending order")
		DESC
	}
}