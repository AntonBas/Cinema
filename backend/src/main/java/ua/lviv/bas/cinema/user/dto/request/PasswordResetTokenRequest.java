package ua.lviv.bas.cinema.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Password reset token to check before showing the new password form")
public record PasswordResetTokenRequest(
        @Schema(description = "Password reset token from the email link", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Token is required")
        String token
) {
}
