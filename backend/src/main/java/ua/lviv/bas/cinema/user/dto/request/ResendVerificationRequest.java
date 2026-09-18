package ua.lviv.bas.cinema.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Resend email verification request")
public record ResendVerificationRequest(
        @Schema(description = "Email to resend the verification link to", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email
) {
}
