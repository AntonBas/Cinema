package ua.lviv.bas.cinema.dto.bonus.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to use bonus points for booking")
public class UseBonusPointsRequest {

	@NotNull(message = "Points amount is required")
	@Min(value = 100, message = "Minimum 100 points required")
	@Max(value = 1000, message = "Maximum 1000 points allowed")
	@Schema(description = "Number of bonus points to use (100-1000)", example = "500", minimum = "100", maximum = "1000")
	private Integer pointsToUse;
}