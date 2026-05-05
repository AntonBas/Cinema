package ua.lviv.bas.cinema.dto.promotion.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to claim a promotion")
public record ClaimPromotionRequest(
        @NotNull(message = "Promotion ID is required")
        @Schema(description = "ID of the promotion to be claimed", example = "1")
        Long promotionId
) {
}