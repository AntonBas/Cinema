package ua.lviv.bas.cinema.dto.promotion.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PromotionRequest(
		@NotBlank(message = "Title is required") @Size(max = 60, message = "Title must not exceed 60 characters") @Schema(description = "Title of the promotion", example = "Summer Special") String title,

		@Size(max = 500, message = "Description must not exceed 500 characters") @Schema(description = "Promotion description", example = "Get bonus points for your first visit") String description,

		@NotNull(message = "Bonus points are required") @Positive(message = "Bonus points must be positive") @Schema(description = "Number of bonus points awarded", example = "100") Integer bonusPoints,

		@Future(message = "Start date must be in the future") @Schema(description = "Start date of the promotion", example = "2024-07-01") LocalDate startDate,

		@Future(message = "End date must be in the future") @Schema(description = "End date of the promotion", example = "2024-07-10") LocalDate endDate) {
}