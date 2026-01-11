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
@Schema(description = "Detailed refund information")
public class RefundDetailsResponse {

	@Schema(description = "Refund ID", example = "1")
	private Long id;

	@Schema(description = "Refund number", example = "RF-2024-0001")
	private String refundNumber;

	@Schema(description = "Refund status", example = "COMPLETED")
	private String status;

	@Schema(description = "User ID", example = "789")
	private Long userId;

	@Schema(description = "User email", example = "user@example.com")
	private String userEmail;

	@Schema(description = "User name", example = "John Doe")
	private String userName;

	@Schema(description = "Payment ID", example = "456")
	private Long paymentId;

	@Schema(description = "Payment reference", example = "TRX-789456123")
	private String paymentReference;

	@Schema(description = "Original payment amount", example = "200.00")
	private BigDecimal paymentAmount;

	@Schema(description = "Total refund amount", example = "180.00")
	private BigDecimal totalAmount;

	@Schema(description = "Total bonus points to refund", example = "90")
	private Integer totalBonusPointsToDeduct;

	@Schema(description = "Refund reason", example = "Schedule conflict")
	private String reason;

	@Schema(description = "Processed by", example = "AUTO_SYSTEM")
	private String processedBy;

	@JsonFormat(pattern = "dd.MM.yyyy HH:mm")
	@Schema(description = "Processing timestamp", example = "15.12.2024 10:20:00")
	private LocalDateTime processedAt;

	@JsonFormat(pattern = "dd.MM.yyyy HH:mm")
	@Schema(description = "Creation timestamp", example = "15.12.2024 10:15:00")
	private LocalDateTime createdAt;

	@JsonFormat(pattern = "dd.MM.yyyy HH:mm")
	@Schema(description = "Last update timestamp", example = "15.12.2024 10:20:00")
	private LocalDateTime updatedAt;

	@Schema(description = "Refund items with details")
	private List<RefundItemDetails> items;

	@Schema(description = "Bonus transactions")
	private List<BonusTransactionResponse> bonusTransactions;

	@Schema(description = "Payment refund status", example = "PROCESSED")
	private String paymentStatus;

	@Schema(description = "Bonus refund status", example = "COMPLETED")
	private String bonusStatus;
}

@Data
@Builder
@Schema(description = "Detailed refund item information")
class RefundItemDetails {

	@Schema(description = "Refund item ID", example = "1")
	private Long id;

	@Schema(description = "Ticket ID", example = "123")
	private Long ticketId;

	@Schema(description = "Ticket code", example = "TK2024000123")
	private String ticketCode;

	@Schema(description = "Movie title", example = "Interstellar")
	private String movieTitle;

	@Schema(description = "Session time", example = "15.12.2024 18:30")
	private LocalDateTime sessionTime;

	@Schema(description = "Seat information", example = "Row 5, Seat 12")
	private String seatInfo;

	@Schema(description = "Ticket price", example = "200.00")
	private BigDecimal ticketPrice;

	@Schema(description = "Refund percentage", example = "90.00")
	private BigDecimal refundPercentage;

	@Schema(description = "Refund amount", example = "180.00")
	private BigDecimal refundAmount;

	@Schema(description = "Fee amount", example = "20.00")
	private BigDecimal feeAmount;

	@Schema(description = "Bonus points used", example = "100")
	private Integer bonusPointsUsed;

	@Schema(description = "Bonus points to refund", example = "90")
	private Integer bonusPointsToDeduct;

	@Schema(description = "Status", example = "COMPLETED")
	private String status;

	@Schema(description = "Creation timestamp", example = "15.12.2024 10:15:00")
	private LocalDateTime createdAt;
}

@Data
@Builder
@Schema(description = "Bonus transaction information")
class BonusTransactionResponse {

	@Schema(description = "Transaction ID", example = "10")
	private Long id;

	@Schema(description = "Points amount", example = "90")
	private Integer points;

	@Schema(description = "Transaction type", example = "REFUND")
	private String type;

	@Schema(description = "Description", example = "Refund for ticket #TK2024000123")
	private String description;

	@Schema(description = "Creation timestamp", example = "15.12.2024 10:20:00")
	private LocalDateTime createdAt;
}