package ua.lviv.bas.cinema.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating user's birth date verification status")
public class VerificationBirthDateRequest {

	@Schema(description = "Verification status for user's birth date", example = "VERIFIED", allowableValues = {
			"VERIFIED", "NOT_VERIFIED" })
	private VerificationStatus verificationStatus;
}