package ua.lviv.bas.cinema.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto {

	@Email(message = "Invalid email format")
	@NotBlank(message = "Email is required")
	private String email;

	@Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
	@NotBlank(message = "First name is required")
	private String firstName;

	@Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
	@NotBlank(message = "Last name is required")
	private String lastName;

	@Past(message = "Date of birth must be in the past")
	@NotNull(message = "Date of birth is required")
	private LocalDate dateOfBirth;

	@Size(min = 2, max = 50, message = "City name must be between 2 and 50 characters")
	@NotBlank(message = "City is required")
	private String city;

	@Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
	@NotBlank(message = "Phone Number is required")
	private String phoneNumber;

	@Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
	@NotBlank(message = "Password is required")
	private String password;

	@NotBlank(message = "Password confirmation is required")
	private String passwordConfirm;

	@AssertTrue(message = "Passwords do not match")
	public boolean isPasswordMatching() {
		if (password == null || passwordConfirm == null) {
			return false;
		}
		return password.equals(passwordConfirm);
	}
}