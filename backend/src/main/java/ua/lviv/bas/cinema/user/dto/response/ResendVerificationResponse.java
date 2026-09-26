package ua.lviv.bas.cinema.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resend verification cooldown status")
public record ResendVerificationResponse(
        @Schema(description = "Seconds until the next resend is allowed, 0 if allowed now", example = "60")
        int cooldownSeconds
) {
}
