package ua.lviv.bas.cinema.promotion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Request to create or update a promotion")
public record PromotionRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 60, message = "Title must not exceed 60 characters")
        @Schema(description = "Title of the promotion", example = "Summer Special")
        String title,

        @Size(max = 150, message = "Description must not exceed 150 characters")
        @Schema(description = "Promotion description", example = "Get bonus points for your first visit")
        String description,

        @NotNull(message = "Bonus points are required")
        @Positive(message = "Bonus points must be positive")
        @Max(value = 10_000, message = "Bonus points must be at most 10,000")
        @Schema(description = "Number of bonus points awarded", example = "100")
        Integer bonusPoints,

        @Schema(description = "Start date of the promotion", example = "2024-07-01")
        LocalDate startDate,

        @Schema(description = "End date of the promotion", example = "2024-07-10")
        LocalDate endDate,

        @Schema(description = "Whether the promotion is enabled, defaults to true", example = "true", nullable = true)
        Boolean active
) {
}
