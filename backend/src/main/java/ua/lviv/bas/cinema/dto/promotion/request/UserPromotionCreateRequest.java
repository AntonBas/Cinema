package ua.lviv.bas.cinema.dto.promotion.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request DTO to claim/activate a promotion")
public class UserPromotionCreateRequest {

	@NotNull(message = "Promotion ID is required")
	@Schema(description = "ID of the promotion to be claimed", example = "1")
	private Long promotionId;
}