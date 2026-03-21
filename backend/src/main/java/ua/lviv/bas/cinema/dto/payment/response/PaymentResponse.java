package ua.lviv.bas.cinema.dto.payment.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;

public record PaymentResponse(@Schema(description = "Payment ID", example = "123") Long id,

		@Schema(description = "Booking ID", example = "456") Long bookingId,

		@Schema(description = "Booking number", example = "BK-20240115-00123") String bookingNumber,

		@Schema(description = "User email", example = "user@example.com") String userEmail,

		@Schema(description = "Movie title", example = "Inception") String movieTitle,

		@Schema(description = "Session time", example = "2024-01-15T18:30:00") LocalDateTime sessionTime,

		@Schema(description = "Hall name", example = "Hall A") String hallName,

		@Schema(description = "Payment amount", example = "1000.00") BigDecimal amount,

		@Schema(description = "Final amount after discounts", example = "950.00") BigDecimal finalAmount,

		@Schema(description = "Payment status", example = "PENDING") PaymentStatus status,

		@Schema(description = "LiqPay order ID", example = "ORDER_ABC123") String liqpayOrderId,

		@Schema(description = "LiqPay payment ID", example = "lp_123456789") String liqpayPaymentId,

		@Schema(description = "Payment time", example = "2024-01-15T14:35:00") LocalDateTime paymentTime,

		@Schema(description = "Error code from LiqPay", example = "error_validation") String errorCode,

		@Schema(description = "Error description from LiqPay", example = "Invalid signature") String errorDescription,

		@Schema(description = "Masked card number", example = "****4832") String senderCardMask,

		@Schema(description = "LiqPay action type", example = "pay") String actionType,

		@Schema(description = "Whether payment can be refunded via API", example = "true") Boolean refundableViaApi,

		@Schema(description = "Created at", example = "2024-01-15T14:30:00") LocalDateTime createdAt,

		@Schema(description = "Updated at", example = "2024-01-15T14:35:00") LocalDateTime updatedAt) {
}