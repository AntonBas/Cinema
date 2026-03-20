package ua.lviv.bas.cinema.dto.booking.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public record BookingTempHoldResponse(@Schema(description = "Temporary booking ID", example = "123") Long bookingId,

		@Schema(description = "Expiration time", example = "2024-01-15T14:35:00") LocalDateTime expiresAt,

		@Schema(description = "Remaining seconds", example = "300") Integer remainingSeconds) {
}