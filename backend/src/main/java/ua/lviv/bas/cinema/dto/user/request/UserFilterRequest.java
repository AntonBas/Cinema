package ua.lviv.bas.cinema.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for filtering users (admin only)")
public class UserFilterRequest {

	@Schema(description = "Search in email, first name or last name", example = "john")
	private String search;

	@Schema(description = "User role filter", example = "ROLE_USER")
	private UserRole role;

	@Schema(description = "Verification status filter", example = "VERIFIED")
	private VerificationStatus verificationStatus;

	@Schema(description = "Enabled status filter", example = "true")
	private Boolean enabled;
}