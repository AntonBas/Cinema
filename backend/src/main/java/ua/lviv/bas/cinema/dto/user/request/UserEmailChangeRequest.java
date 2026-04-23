package ua.lviv.bas.cinema.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserEmailChangeRequest(
        @Schema(description = "New email address for the user", example = "new.email@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @Email(message = "Invalid email format")
        @NotBlank(message = "New email is required")
        String newEmail,

        @Schema(description = "Current password for verification", example = "CurrentPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password confirmation is required")
        String password
) {
}