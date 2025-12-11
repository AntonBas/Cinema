package ua.lviv.bas.cinema.dto.user.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
@Schema(description = "Response DTO for user list in admin panel")
public class AdminUserListResponse {

	@Schema(description = "Unique identifier of the user", example = "1")
	private Long id;

	@Schema(description = "User's email address", example = "john.doe@example.com")
	private String email;

	@Schema(description = "User's first name", example = "John")
	private String firstName;

	@Schema(description = "User's last name", example = "Doe")
	private String lastName;

	@Schema(description = "User's role", example = "CUSTOMER", allowableValues = { "ADMIN", "MANAGER", "CUSTOMER" })
	private UserRole userRole;

	@Schema(description = "Account enabled status", example = "true")
	private boolean enabled;

	@Schema(description = "Birth date verification status", example = "VERIFIED")
	private VerificationStatus verificationStatus;

	@Schema(description = "Date and time when birth date was verified")
	private LocalDateTime verifiedAt;

	@Schema(description = "Date when the account was created", example = "2024-01-10", type = "string", format = "date")
	private LocalDate createdAt;

	@Schema(description = "Date when the account was last updated", example = "2024-01-15", type = "string", format = "date")
	private LocalDate updatedAt;

	@Schema(description = "Number of tickets purchased by the user", example = "15")
	private int ticketsCount;

	@Schema(description = "Date of user's last activity", example = "2024-01-15", type = "string", format = "date")
	private LocalDate lastActivity;
}