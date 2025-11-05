package ua.lviv.bas.cinema.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordChangeDto {

	@NotBlank(message = "Current password is required")
	private String currentPassword;

	@Size(min = 8, max = 72, message = "New password must be between 8 and 72 characters")
	@NotBlank(message = "New password is required")
	private String newPassword;

	@NotBlank(message = "Password confirmation is required")
	private String passwordConfirm;
}