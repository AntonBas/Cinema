package ua.lviv.bas.cinema.refund.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Public refund policy")
public record RefundPolicyResponse(
        @Schema(description = "Refund tiers, from the most generous to none")
        List<RefundPolicyRuleResponse> rules,

        @Schema(description = "How long the refunded money takes to arrive", example = "5-7 business days")
        String processingTime,

        @Schema(description = "Support contact for refund questions", example = "support@example.com")
        String contactEmail
) {
}
