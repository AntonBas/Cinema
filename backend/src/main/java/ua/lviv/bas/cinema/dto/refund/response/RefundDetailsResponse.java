package ua.lviv.bas.cinema.dto.refund.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

public record RefundDetailsResponse(@Schema(description = "Refund ID", example = "1") Long id,

		@Schema(description = "Refund number", example = "RF-2024-0001") String refundNumber,

		@Schema(description = "Refund status", example = "COMPLETED") String status,

		@Schema(description = "User ID", example = "789") Long userId,

		@Schema(description = "User email", example = "user@example.com") String userEmail,

		@Schema(description = "User name", example = "John Doe") String userName,

		@Schema(description = "Payment ID", example = "456") Long paymentId,

		@Schema(description = "Payment reference", example = "TRX-789456123") String paymentReference,

		@Schema(description = "Original payment amount", example = "200.00") BigDecimal paymentAmount,

		@Schema(description = "Total refund amount", example = "180.00") BigDecimal totalAmount,

		@Schema(description = "Total bonus points to refund", example = "90") Integer totalBonusPointsToDeduct,

		@Schema(description = "Refund reason", example = "Schedule conflict") String reason,

		@Schema(description = "Processed by", example = "AUTO_SYSTEM") String processedBy,

		@JsonFormat(pattern = "dd.MM.yyyy HH:mm") @Schema(description = "Processing timestamp", example = "15.12.2024 10:20:00") LocalDateTime processedAt,

		@JsonFormat(pattern = "dd.MM.yyyy HH:mm") @Schema(description = "Creation timestamp", example = "15.12.2024 10:15:00") LocalDateTime createdAt,

		@JsonFormat(pattern = "dd.MM.yyyy HH:mm") @Schema(description = "Last update timestamp", example = "15.12.2024 10:20:00") LocalDateTime updatedAt,

		@Schema(description = "Refund items with details") List<RefundItemDetails> items,

		@Schema(description = "Bonus transactions") List<BonusTransactionInfo> bonusTransactions,

		@Schema(description = "Payment refund status", example = "PROCESSED") String paymentStatus,

		@Schema(description = "Bonus refund status", example = "COMPLETED") String bonusStatus) {

	public record RefundItemDetails(@Schema(description = "Refund item ID", example = "1") Long id,

			@Schema(description = "Ticket ID", example = "123") Long ticketId,

			@Schema(description = "Ticket code", example = "TK2024000123") String ticketCode,

			@Schema(description = "Movie title", example = "Interstellar") String movieTitle,

			@Schema(description = "Session time", example = "15.12.2024 18:30") LocalDateTime sessionTime,

			@Schema(description = "Seat information", example = "Row 5, Seat 12") String seatInfo,

			@Schema(description = "Ticket price", example = "200.00") BigDecimal ticketPrice,

			@Schema(description = "Refund percentage", example = "90.00") BigDecimal refundPercentage,

			@Schema(description = "Refund amount", example = "180.00") BigDecimal refundAmount,

			@Schema(description = "Fee amount", example = "20.00") BigDecimal feeAmount,

			@Schema(description = "Bonus points used", example = "100") Integer bonusPointsUsed,

			@Schema(description = "Bonus points to refund", example = "90") Integer bonusPointsToDeduct,

			@Schema(description = "Status", example = "COMPLETED") String status,

			@Schema(description = "Creation timestamp", example = "15.12.2024 10:15:00") LocalDateTime createdAt) {
	}

	public record BonusTransactionInfo(@Schema(description = "Transaction ID", example = "10") Long id,

			@Schema(description = "Points amount", example = "90") Integer points,

			@Schema(description = "Transaction type", example = "REFUND") String type,

			@Schema(description = "Description", example = "Refund for ticket #TK2024000123") String description,

			@Schema(description = "Creation timestamp", example = "15.12.2024 10:20:00") LocalDateTime createdAt) {
	}
}