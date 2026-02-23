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
@Schema(description = "Request DTO for creating a movie session")
public class SessionCreateRequest {

	@Schema(description = "Start time of the movie session", example = "2024-01-15T18:30:00")
	@FutureOrPresent
	@NotNull
	private LocalDateTime startTime;

	@Schema(description = "Base price for a standard seat", example = "150.00")
	@Positive
	@DecimalMin("10.0")
	@NotNull
	private BigDecimal basePrice;

	@Schema(description = "ID of the movie being shown", example = "1")
	@NotNull
	private Long movieId;

	@Schema(description = "ID of the cinema hall", example = "2")
	@NotNull
	private Long hallId;
}