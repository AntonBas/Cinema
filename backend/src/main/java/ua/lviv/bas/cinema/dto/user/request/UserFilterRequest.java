package ua.lviv.bas.cinema.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.user.UserRole;
import ua.lviv.bas.cinema.domain.user.VerificationStatus;

public record UserFilterRequest(
		@Schema(description = "Search in email, first name or last name", example = "john") String search,

		@Schema(description = "User role filter", example = "ROLE_USER") UserRole role,

		@Schema(description = "Verification status filter", example = "VERIFIED") VerificationStatus verificationStatus,

		@Schema(description = "Enabled status filter", example = "true") Boolean enabled) {
}