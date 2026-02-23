package ua.lviv.bas.cinema.dto.session.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for filtering cinema sessions")
public class SessionFilterRequest {

	@Schema(description = "Filter by session status", example = "SCHEDULED")
	private CinemaSessionStatus status;

	@Schema(description = "Filter by start date (from)", example = "2024-01-01")
	private LocalDate dateFrom;

	@Schema(description = "Filter by start date (to)", example = "2024-01-31")
	private LocalDate dateTo;

	@Schema(description = "Filter by cinema hall ID", example = "1")
	private Long hallId;

	@Schema(description = "Filter by movie title", example = "Inception")
	private String movieTitle;

	@AssertTrue(message = "dateTo must be after or equal to dateFrom")
	public boolean isValidDateRange() {
		if (dateFrom == null || dateTo == null)
			return true;
		return !dateTo.isBefore(dateFrom);
	}
}