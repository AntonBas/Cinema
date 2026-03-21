package ua.lviv.bas.cinema.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ua.lviv.bas.cinema.domain.enums.UserRole;

public record UserRoleUpdateRequest(
		@Schema(description = "New role for the user", example = "ROLE_ADMIN", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Role is required") UserRole userRole) {
}