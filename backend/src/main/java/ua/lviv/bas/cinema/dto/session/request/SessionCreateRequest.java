package ua.lviv.bas.cinema.dto.session.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating or updating a movie session")
public class SessionCreateRequest {

	@Schema(description = "Start time of the movie session", example = "2024-01-15T18:30:00", requiredMode = Schema.RequiredMode.REQUIRED, type = "string", format = "date-time")
	@FutureOrPresent
	@NotNull(message = "Start time is required")
	private LocalDateTime startTime;

	@Schema(description = "Base price for a standard seat in this session", example = "150.00", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "10.0")
	@Positive(message = "Base price must be positive")
	@DecimalMin(value = "10.0", message = "Base price must be at least 10 UAH")
	@NotNull(message = "Base price is required")
	private BigDecimal basePrice;

	@Schema(description = "ID of the movie being shown", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "Movie ID is required")
	private Long movieId;

	@Schema(description = "ID of the cinema hall where the session takes place", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "Hall ID is required")
	private Long hallId;
}