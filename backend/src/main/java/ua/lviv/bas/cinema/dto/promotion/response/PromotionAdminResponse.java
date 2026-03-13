package ua.lviv.bas.cinema.dto.promotion.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin list view DTO for promotion (without description)")
public class PromotionAdminResponse {

	@Schema(description = "Promotion ID", example = "1")
	private Long id;

	@Schema(description = "Title of the promotion", example = "Summer Special")
	private String title;

	@Schema(description = "Number of bonus points awarded", example = "100")
	private Integer bonusPoints;

	@Schema(description = "Start date of the promotion", example = "2024-07-01")
	private LocalDate startDate;

	@Schema(description = "End date of the promotion", example = "2024-07-10")
	private LocalDate endDate;
}