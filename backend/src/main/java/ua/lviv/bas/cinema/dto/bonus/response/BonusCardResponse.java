package ua.lviv.bas.cinema.dto.bonus.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "User's bonus card information")
public class BonusCardResponse {

	@Schema(description = "Bonus card ID", example = "1")
	private Long id;

	@Schema(description = "Current points balance", example = "250")
	private Integer pointsBalance;

	@Schema(description = "Date when last birthday bonus was awarded", example = "2025-08-21", nullable = true)
	private LocalDate lastBirthdayBonusDate;

	@Schema(description = "Whether welcome bonus has been received", example = "true")
	private Boolean welcomeBonusReceived;

	@Schema(description = "User ID associated with this bonus card", example = "21")
	private Long userId;

}
