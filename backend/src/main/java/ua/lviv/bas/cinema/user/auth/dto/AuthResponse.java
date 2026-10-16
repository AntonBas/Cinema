package ua.lviv.bas.cinema.user.auth.dto;

import ua.lviv.bas.cinema.user.dto.UserResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response — the JWT itself is only set as an httpOnly cookie, never in the body")
public record AuthResponse(
        @Schema(description = "User profile information")
        UserResponse user
) {
}
