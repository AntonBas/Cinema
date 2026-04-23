package ua.lviv.bas.cinema.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.user.UserRole;
import ua.lviv.bas.cinema.domain.user.VerificationStatus;

import java.time.LocalDateTime;

public record AdminUserListResponse(
        @Schema(description = "Unique identifier of the user", example = "1")
        Long id,

        @Schema(description = "User's email address", example = "john.doe@example.com")
        String email,

        @Schema(description = "User's first name", example = "John")
        String firstName,

        @Schema(description = "User's last name", example = "Doe")
        String lastName,

        @Schema(description = "User's role", example = "ROLE_USER")
        UserRole userRole,

        @Schema(description = "Account enabled status", example = "true")
        boolean enabled,

        @Schema(description = "Birth date verification status", example = "VERIFIED")
        VerificationStatus verificationStatus,

        @Schema(description = "Date and time when birth date was verified")
        LocalDateTime verifiedAt,

        @Schema(description = "Number of tickets purchased by the user", example = "15")
        Long ticketsCount,

        @Schema(description = "Date of user's last activity", example = "2024-01-15")
        LocalDateTime lastActivity
) {
}