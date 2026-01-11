package ua.lviv.bas.cinema.dto.refund.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after successful refund processing")
public class RefundResponse {

	@Schema(description = "Refund ID", example = "1")
	private Long id;

	@Schema(description = "Refund number", example = "RF-2024-0001")
	private String refundNumber;

	@Schema(description = "Refund status", example = "COMPLETED")
	private String status;

	@Schema(description = "Total refund amount", example = "180.00")
	private BigDecimal totalAmount;

	@Schema(description = "Total bonus points to refund", example = "90")
	private Integer totalBonusPointsToDeduct;

	@Schema(description = "Refund reason", example = "Cannot attend")
	private String reason;

	@Schema(description = "Processed by", example = "AUTO_SYSTEM")
	private String processedBy;

	@JsonFormat(pattern = "dd.MM.yyyy HH:mm")
	@Schema(description = "Processing timestamp", example = "15.12.2024 10:20:00")
	private LocalDateTime processedAt;

	@JsonFormat(pattern = "dd.MM.yyyy HH:mm")
	@Schema(description = "Creation timestamp", example = "15.12.2024 10:15:00")
	private LocalDateTime createdAt;

	@Schema(description = "Payment ID", example = "456")
	private Long paymentId;

	@Schema(description = "Payment method", example = "CARD")
	private String paymentMethod;

	@Schema(description = "Refund items")
	private List<RefundItemResponse> items;

	@Schema(description = "Message for user", example = "Refund processed successfully")
	private String message;

	@Schema(description = "Estimated refund time", example = "3-5 business days")
	private String estimatedRefundTime;
}