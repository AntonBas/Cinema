package ua.lviv.bas.cinema.dto.refund.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
@Schema(description = "Response with refund preview information")
public class RefundPreviewResponse {

	@Schema(description = "Ticket ID", example = "123")
	private Long ticketId;

	@Schema(description = "Unique ticket code", example = "TK2024000123")
	private String ticketCode;

	@Schema(description = "Movie title", example = "Interstellar")
	private String movieTitle;

	@JsonFormat(pattern = "dd.MM.yyyy HH:mm")
	@Schema(description = "Session time", example = "15.12.2024 18:30")
	private LocalDateTime sessionTime;

	@Schema(description = "Hall name", example = "Hall 1")
	private String hallName;

	@Schema(description = "Seat information", example = "Row 5, Seat 12")
	private String seatInfo;

	@Schema(description = "Original ticket price", example = "200.00")
	private BigDecimal originalPrice;

	@Schema(description = "Final price with discount", example = "200.00")
	private BigDecimal finalPrice;

	@Schema(description = "Refund amount", example = "180.00")
	private BigDecimal refundAmount;

	@Schema(description = "Refund percentage", example = "90.00")
	private BigDecimal refundPercentage;

	@Schema(description = "Fee/retention amount", example = "20.00")
	private BigDecimal feeAmount;

	@Schema(description = "Fee percentage", example = "10.00")
	private BigDecimal feePercentage;

	@Schema(description = "Bonus points used", example = "100")
	private Integer bonusPointsUsed;

	@Schema(description = "Bonus points to refund", example = "90")
	private Integer bonusPointsToRefund;

	@Schema(description = "Refund policy name", example = "Standard Refund")
	private String policyName;

	@Schema(description = "Policy description", example = "90% refund 2-24 hours before session")
	private String policyDescription;

	@Schema(description = "Whether ticket is refundable", example = "true")
	private Boolean isRefundable;

	@Schema(description = "Reason if not refundable")
	private String nonRefundableReason;

	@JsonFormat(pattern = "dd.MM.yyyy HH:mm")
	@Schema(description = "Refund deadline", example = "15.12.2024 16:30")
	private LocalDateTime refundDeadline;

	@Schema(description = "Time remaining", example = "2 hours 30 minutes")
	private String remainingTime;

	@Schema(description = "Purchase time", example = "15.12.2024 10:15")
	private String purchaseTime;

	@Schema(description = "Ticket type", example = "Standard")
	private String ticketType;
}