package ua.lviv.bas.cinema.dto.promotion.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request DTO for creating a promotion")
public class PromotionUpdateRequest {

	@NotBlank(message = "Title is required")
	@Size(max = 60, message = "Title must not exceed 60 characters")
	@Schema(description = "Title of the promotion", example = "Summer Special - Updated")
	private String title;

	@Size(max = 500, message = "Description must not exceed 500 characters")
	@Schema(description = "Promotion description", example = "Updated description: Get bonus points for your first visit in the new year")
	private String description;

	@NotNull(message = "Bonus points are required")
	@Positive(message = "Bonus points must be positive")
	@Schema(description = "Number of bonus points awarded", example = "100")
	private Integer bonusPoints;

	@Future(message = "Start date must be in the future")
	@Schema(description = "Start date of the promotion", example = "2024-07-01T00:00:00")
	private LocalDate startDate;

	@Future(message = "End date must be in the future")
	@Schema(description = "End date of the promotion", example = "2024-07-10T00:00:00")
	private LocalDate endDate;
}