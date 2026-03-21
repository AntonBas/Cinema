package ua.lviv.bas.cinema.dto.promotion.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

public record PromotionResponse(@Schema(description = "Promotion ID", example = "1") Long id,

		@Schema(description = "Title of the promotion", example = "Summer Special") String title,

		@Schema(description = "Promotion description", example = "Get bonus points for your first visit in the new year") String description,

		@Schema(description = "Number of bonus points awarded", example = "100") Integer bonusPoints,

		@Schema(description = "Start date of the promotion", example = "2024-07-01T00:00:00") LocalDate startDate,

		@Schema(description = "End date of the promotion", example = "2024-07-10T00:00:00") LocalDate endDate) {
}