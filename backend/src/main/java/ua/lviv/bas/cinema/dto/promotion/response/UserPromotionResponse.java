package ua.lviv.bas.cinema.dto.promotion.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after claiming/activating a promotion for a user")
public class UserPromotionResponse {

	@Schema(description = "Redemption record ID", example = "1")
	private Long id;

	@Schema(description = "Promotion ID", example = "1")
	private Long promotionId;

	@Schema(description = "Title of the promotion", example = "Summer Special")
	private String promotionTitle;

	@Schema(description = "Date and time when the promotion was claimed", example = "2024-07-01T12:00:00")
	private LocalDateTime claimedAt;

	@Schema(description = "Number of points awarded to the user", example = "100")
	private Integer pointsAwarded;

	@Schema(description = "User's new balance after applying the promotion", example = "1500")
	private Integer newBalance;
}