package ua.lviv.bas.cinema.dto.bonus.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request to use bonus points for purchase")
public class UseBonusPointsRequest {

	@NotNull(message = "Points amount is required")
	@Min(value = 50, message = "Minimum 50 points required")
	@Max(value = 300, message = "Maximum 300 points allowed")
	@Schema(description = "Number of bonus points to use (50-300)", example = "150", minimum = "50", maximum = "300")
	private Integer pointsToUse;
}