package ua.lviv.bas.cinema.refund.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "One tier of the refund policy")
public record RefundPolicyRuleResponse(
        @Schema(description = "Tier name", example = "Full Refund")
        String name,

        @Schema(description = "Tier description", example = "100% refund — 48+ hours before the session")
        String description,

        @Schema(description = "Refunded percentage of the ticket price", example = "100")
        int percentage,

        @Schema(description = "When the tier applies", example = "More than 48 hours before session start")
        String condition
) {
}
