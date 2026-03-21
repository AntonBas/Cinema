package ua.lviv.bas.cinema.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserPasswordUpdateRequest(
		@Schema(description = "Current password for verification", example = "OldPassword123!", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Current password is required") String currentPassword,

		@Schema(description = "New password", example = "NewSecurePassword123!", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8, maxLength = 72) @Size(min = 8, max = 32, message = "New password must be between 8 and 32 characters") @NotBlank(message = "New password is required") String newPassword,

		@Schema(description = "Confirmation of the new password", example = "NewSecurePassword123!", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Password confirmation is required") String passwordConfirm) {
}