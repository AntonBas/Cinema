package ua.lviv.bas.cinema.dto.session.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating a movie session")
public class SessionUpdateRequest {

	@Schema(description = "Start time of the movie session", example = "2024-01-15T19:00:00")
	private LocalDateTime startTime;

	@Schema(description = "Base price for a standard seat", example = "160.00")
	private BigDecimal basePrice;

	@Schema(description = "ID of the movie being shown", example = "1")
	private Long movieId;

	@Schema(description = "ID of the cinema hall", example = "2")
	private Long hallId;
}