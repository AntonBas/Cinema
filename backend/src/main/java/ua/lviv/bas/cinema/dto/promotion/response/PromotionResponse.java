package ua.lviv.bas.cinema.dto.promotion.response;

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
@Schema(description = "Response DTO for promotion details")
public class PromotionResponse {

	@Schema(description = "Promotion ID", example = "1")
	private Long id;

	@Schema(description = "Title of the promotion", example = "Summer Special")
	private String title;

	@Schema(description = "Promotion description", example = "Get bonus points for your first visit in the new year")
	private String description;

	@Schema(description = "Number of bonus points awarded", example = "100")
	private Integer bonusPoints;

	@Schema(description = "Start date of the promotion", example = "2024-07-01T00:00:00")
	private LocalDateTime startDate;

	@Schema(description = "End date of the promotion", example = "2024-07-10T00:00:00")
	private LocalDateTime endDate;
}