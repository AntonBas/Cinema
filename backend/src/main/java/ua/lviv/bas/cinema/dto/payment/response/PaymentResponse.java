package ua.lviv.bas.cinema.dto.payment.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.booking.status.PaymentStatus;

public record PaymentResponse(
		@Schema(description = "Booking number", example = "BK-20240115-00123") String bookingNumber,

		@Schema(description = "Movie title", example = "Inception") String movieTitle,

		@Schema(description = "Session time", example = "2024-01-15T18:30:00") LocalDateTime sessionTime,

		@Schema(description = "Hall name", example = "Hall A") String hallName,

		@Schema(description = "Final amount after discounts", example = "950.00") BigDecimal finalAmount,

		@Schema(description = "Payment status", example = "SUCCESS") PaymentStatus status,

		@Schema(description = "Payment time", example = "2024-01-15T14:35:00") LocalDateTime paymentTime,

		@Schema(description = "Masked card number", example = "****4832") String senderCardMask,

		@Schema(description = "Error description from LiqPay", example = "Invalid signature") String errorDescription) {
}