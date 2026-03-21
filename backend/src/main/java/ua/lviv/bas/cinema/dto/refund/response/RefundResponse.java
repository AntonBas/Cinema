package ua.lviv.bas.cinema.dto.refund.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

public record RefundResponse(@Schema(description = "Refund ID", example = "1") Long id,

		@Schema(description = "Refund number", example = "RF-2024-0001") String refundNumber,

		@Schema(description = "Refund status", example = "COMPLETED") String status,

		@Schema(description = "Total refund amount", example = "180.00") BigDecimal totalAmount,

		@Schema(description = "Total bonus points to refund", example = "90") Integer totalBonusPointsToDeduct,

		@Schema(description = "Refund reason", example = "Cannot attend") String reason,

		@Schema(description = "Processed by", example = "AUTO_SYSTEM") String processedBy,

		@JsonFormat(pattern = "dd.MM.yyyy HH:mm") @Schema(description = "Processing timestamp", example = "15.12.2024 10:20:00") LocalDateTime processedAt,

		@JsonFormat(pattern = "dd.MM.yyyy HH:mm") @Schema(description = "Creation timestamp", example = "15.12.2024 10:15:00") LocalDateTime createdAt,

		@Schema(description = "Payment ID", example = "456") Long paymentId,

		@Schema(description = "Payment method", example = "CARD") String paymentMethod,

		@Schema(description = "Refund items") List<RefundItemResponse> items,

		@Schema(description = "Message for user", example = "Refund processed successfully") String message,

		@Schema(description = "Estimated refund time", example = "3-5 business days") String estimatedRefundTime) {
}