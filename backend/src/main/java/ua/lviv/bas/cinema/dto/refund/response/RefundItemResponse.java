package ua.lviv.bas.cinema.dto.refund.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Refund item details for a specific ticket")
public record RefundItemResponse(
        @Schema(description = "Refund item ID", example = "1")
        Long id,

        @Schema(description = "Ticket ID", example = "123")
        Long ticketId,

        @Schema(description = "Ticket code", example = "TK2024000123")
        String ticketCode,

        @Schema(description = "Ticket price", example = "200.00")
        BigDecimal ticketPrice,

        @Schema(description = "Refund percentage", example = "90.00")
        BigDecimal refundPercentage,

        @Schema(description = "Refund amount", example = "180.00")
        BigDecimal refundAmount,

        @Schema(description = "Bonus points to deduct", example = "90")
        Integer bonusPointsToDeduct,

        @Schema(description = "Status", example = "COMPLETED")
        String status
) {
}