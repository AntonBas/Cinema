package ua.lviv.bas.cinema.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ua.lviv.bas.cinema.user.domain.UserRole;

@Schema(description = "Request to update user role")
public record UserRoleUpdateRequest(
        @Schema(description = "New role for the user", example = "ROLE_ADMIN", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Role is required")
        UserRole userRole
) {
}