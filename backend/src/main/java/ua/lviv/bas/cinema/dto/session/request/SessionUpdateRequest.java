package ua.lviv.bas.cinema.dto.session.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating a movie session")
public class SessionUpdateRequest {

	@Schema(description = "Start time of the movie session", example = "2024-01-15T19:00:00")
	@FutureOrPresent(message = "Start time must be in the present or future")
	private LocalDateTime startTime;

	@Schema(description = "Base price for a standard seat", example = "160.00")
	@DecimalMin(value = "10.0", message = "Base price must be at least 10 UAH")
	@Positive(message = "Base price must be positive")
	private BigDecimal basePrice;

	@Schema(description = "ID of the movie being shown", example = "1")
	private Long movieId;

	@Schema(description = "ID of the cinema hall", example = "2")
	private Long hallId;

	@Schema(description = "Status of the session", example = "ONGOING", allowableValues = { "SCHEDULED", "ONGOING",
			"COMPLETED", "CANCELLED" })
	private CinemaSessionStatus status;
}