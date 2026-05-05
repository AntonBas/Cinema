package ua.lviv.bas.cinema.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login response with JWT token and user profile")
public record LoginResponse(
        @Schema(description = "JWT token for authentication")
        String token,

        @Schema(description = "Token type", example = "Bearer")
        String tokenType,

        @Schema(description = "User profile information")
        UserResponse user
) {
}