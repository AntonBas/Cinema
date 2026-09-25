package ua.lviv.bas.cinema.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to set a new password with a password reset token")
public record PasswordResetRequest(
        @Schema(description = "Password reset token from the email link", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Token is required")
        String token,

        @Schema(description = "New password", example = "NewSecurePassword123!", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8, maxLength = 32)
        @Size(min = 8, max = 32, message = "New password must be between 8 and 32 characters")
        @NotBlank(message = "New password is required")
        String newPassword
) {
}
