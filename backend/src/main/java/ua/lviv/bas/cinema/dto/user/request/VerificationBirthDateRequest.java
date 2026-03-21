package ua.lviv.bas.cinema.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;

public record VerificationBirthDateRequest(
		@Schema(description = "Verification status for user's birth date", example = "VERIFIED", allowableValues = {
				"VERIFIED", "NOT_VERIFIED" }) VerificationStatus verificationStatus) {
}