package ua.lviv.bas.cinema.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for changing user's email address")
public class UserEmailChangeRequest {

	@Schema(description = "New email address for the user", example = "new.email@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
	@Email(message = "Invalid email format")
	@NotBlank(message = "New email is required")
	private String newEmail;

	@Schema(description = "Current password for verification", example = "CurrentPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "Password confirmation is required")
	private String password;
}