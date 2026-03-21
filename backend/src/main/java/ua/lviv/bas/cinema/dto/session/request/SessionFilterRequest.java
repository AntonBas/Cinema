package ua.lviv.bas.cinema.dto.session.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;

public record SessionFilterRequest(
		@Schema(description = "Filter by session status", example = "SCHEDULED") CinemaSessionStatus status,

		@Schema(description = "Filter by start date (from)", example = "2024-01-01") LocalDate dateFrom,

		@Schema(description = "Filter by start date (to)", example = "2024-01-31") LocalDate dateTo,

		@Schema(description = "Filter by cinema hall ID", example = "1") Long hallId,

		@Schema(description = "Filter by movie title", example = "Inception") String movieTitle) {
	@AssertTrue(message = "dateTo must be after or equal to dateFrom")
	public boolean isValidDateRange() {
		if (dateFrom == null || dateTo == null) {
			return true;
		}
		return !dateTo.isBefore(dateFrom);
	}
}