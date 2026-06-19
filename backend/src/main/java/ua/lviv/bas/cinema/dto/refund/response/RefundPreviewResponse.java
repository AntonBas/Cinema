package ua.lviv.bas.cinema.dto.refund.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Refund preview with calculated amounts and policy details")
public record RefundPreviewResponse(
        @Schema(description = "Ticket ID", example = "123")
        Long ticketId,

        @Schema(description = "Unique ticket code", example = "TK2024000123")
        String ticketCode,

        @Schema(description = "Movie title", example = "Interstellar")
        String movieTitle,

        @JsonFormat(pattern = "dd.MM.yyyy HH:mm")
        @Schema(description = "Session time", example = "15.12.2024 18:30")
        LocalDateTime sessionTime,

        @Schema(description = "Hall name", example = "Hall 1")
        String hallName,

        @Schema(description = "Seat information", example = "Row 5, Seat 12")
        String seatInfo,

        @Schema(description = "Original ticket price", example = "200.00")
        BigDecimal originalPrice,

        @Schema(description = "Final price with discount", example = "200.00")
        BigDecimal finalPrice,

        @Schema(description = "Refund amount", example = "180.00")
        BigDecimal refundAmount,

        @Schema(description = "Refund percentage", example = "90.00")
        BigDecimal refundPercentage,

        @Schema(description = "Fee/retention amount", example = "20.00")
        BigDecimal feeAmount,

        @Schema(description = "Fee percentage", example = "10.00")
        BigDecimal feePercentage,

        @Schema(description = "Bonus points used", example = "100")
        Integer bonusPointsUsed,

        @Schema(description = "Bonus points to refund", example = "90")
        Integer bonusPointsToRefund,

        @Schema(description = "Refund policy name", example = "Standard Refund")
        String policyName,

        @Schema(description = "Policy description", example = "90% refund 2-24 hours before session")
        String policyDescription,

        @Schema(description = "Whether ticket is refundable", example = "true")
        Boolean isRefundable,

        @Schema(description = "Reason if not refundable")
        String nonRefundableReason,

        @JsonFormat(pattern = "dd.MM.yyyy HH:mm")
        @Schema(description = "Refund deadline", example = "15.12.2024 16:30")
        LocalDateTime refundDeadline,

        @Schema(description = "Time remaining", example = "2 hours 30 minutes")
        String remainingTime,

        @Schema(description = "Purchase time", example = "15.12.2024 10:15")
        String purchaseTime,

        @Schema(description = "Ticket type", example = "Standard")
        String ticketType
) {
}