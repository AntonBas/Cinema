package ua.lviv.bas.cinema.dto.user;

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
public class UserEmailChangeRequest {

	@Email(message = "Invalid email format")
	@NotBlank(message = "New email is required")
	private String newEmail;

	@NotBlank(message = "Password confirmation is required")
	private String password;
}