package ua.lviv.bas.cinema.dto.filter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionFilter {

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime startTime;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime endTime;

	private Long hallId;
	private Long movieId;

	@Builder.Default
	@Min(value = 0, message = "Page must be greater than or equal to 0")
	private int page = 0;

	@Builder.Default
	@Min(value = 1, message = "Size must be greater than or equal to 1")
	@Max(value = 100, message = "Size must be less than or equal to 100")
	private int size = 20;

	@Builder.Default
	private String sortBy = "startTime";

	@Builder.Default
	private SortDirection sortDirection = SortDirection.ASC;

	public enum SortDirection {
		ASC, DESC
	}

	public void validate() {
		if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
			throw new IllegalArgumentException("startTime cannot be after endTime");
		}
	}
}