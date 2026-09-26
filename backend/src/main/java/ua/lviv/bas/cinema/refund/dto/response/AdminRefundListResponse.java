package ua.lviv.bas.cinema.refund.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.refund.domain.status.RefundStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Schema(description = "Refund row in the admin refunds list")
public record AdminRefundListResponse(
        @Schema(description = "Refund ID", example = "15")
        Long id,

        @Schema(description = "Refund status", example = "PROCESSING")
        RefundStatus status,

        @Schema(description = "Refund creation time, as UTC instant", example = "2026-01-15T14:30:00Z")
        Instant createdDate,

        @Schema(description = "Last status change time, as UTC instant", example = "2026-01-15T14:31:00Z")
        Instant lastModifiedDate,

        @Schema(description = "Customer user ID", example = "42")
        Long userId,

        @Schema(description = "Customer email", example = "john.doe@example.com")
        String userEmail,

        @Schema(description = "Booking ID", example = "123")
        Long bookingId,

        @Schema(description = "Booking number", example = "BK-2026-00123")
        String bookingNumber,

        @Schema(description = "Movie title", example = "Inception")
        String movieTitle,

        @Schema(description = "Session start time", example = "2026-01-15T18:30:00")
        LocalDateTime sessionTime,

        @Schema(description = "Refunded ticket code", example = "TKT-3F2A9C1B7D4E")
        String ticketCode,

        @Schema(description = "Refund amount", example = "250.00")
        BigDecimal totalAmount,

        @Schema(description = "Bonus points deducted", example = "25")
        Integer totalBonusPointsToDeduct,

        @Schema(description = "Refund reason given by the customer", example = "Cannot attend")
        String reason,

        @Schema(description = "Payment ID", example = "77")
        Long paymentId,

        @Schema(description = "LiqPay order ID", example = "ORD_ABC123")
        String liqpayOrderId
) {
}
