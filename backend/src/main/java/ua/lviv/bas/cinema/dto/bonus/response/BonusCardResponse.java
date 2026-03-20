package ua.lviv.bas.cinema.dto.bonus.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

public record BonusCardResponse(@Schema(description = "Bonus card ID", example = "1") Long id,

		@Schema(description = "Current points balance", example = "250") Integer pointsBalance,

		@Schema(description = "Date when last birthday bonus was awarded", example = "2025-08-21") LocalDate lastBirthdayBonusDate,

		@Schema(description = "Whether welcome bonus has been received", example = "true") Boolean welcomeBonusReceived,

		@Schema(description = "User ID associated with this bonus card", example = "21") Long userId) {
}