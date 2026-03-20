package ua.lviv.bas.cinema.dto.bonus.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UseBonusPointsRequest(
		@NotNull(message = "Points amount is required") @Min(value = 100, message = "Minimum 100 points required") @Max(value = 100000, message = "Maximum 100000 points allowed") @Schema(description = "Number of bonus points to use (100-100000)", example = "500", minimum = "100", maximum = "100000") Integer pointsToUse) {
}