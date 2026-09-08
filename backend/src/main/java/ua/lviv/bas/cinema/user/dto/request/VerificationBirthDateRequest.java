package ua.lviv.bas.cinema.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ua.lviv.bas.cinema.user.domain.VerificationStatus;

@Schema(description = "Request to update user verification status")
public record VerificationBirthDateRequest(
        @NotNull(message = "Verification status is required")
        @Schema(description = "Verification status for user's birth date", example = "VERIFIED", allowableValues = {"VERIFIED", "NOT_VERIFIED"})
        VerificationStatus verificationStatus
) {
}