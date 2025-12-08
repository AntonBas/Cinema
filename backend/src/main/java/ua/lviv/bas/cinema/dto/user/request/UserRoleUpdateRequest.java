package ua.lviv.bas.cinema.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.UserRole;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating user role")
public class UserRoleUpdateRequest {

	@Schema(description = "New role for the user", example = "MANAGER", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {
			"ADMIN", "MANAGER", "CUSTOMER" })
	@NotNull(message = "Role is required")
	private UserRole userRole;
}