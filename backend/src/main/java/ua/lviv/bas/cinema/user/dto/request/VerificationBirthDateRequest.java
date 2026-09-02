package ua.lviv.bas.cinema.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.user.domain.VerificationStatus;

@Schema(description = "Request to update user verification status")
public record VerificationBirthDateRequest(
        @Schema(description = "Verification status for user's birth date", example = "VERIFIED", allowableValues = {"VERIFIED", "NOT_VERIFIED"})
        VerificationStatus verificationStatus
) {
}