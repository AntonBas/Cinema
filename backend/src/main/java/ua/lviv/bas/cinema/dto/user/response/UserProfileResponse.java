package ua.lviv.bas.cinema.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for user profile information")
public class UserProfileResponse {

	@Schema(description = "Unique identifier of the user", example = "1")
	private Long id;

	@Schema(description = "User's email address", example = "john.doe@example.com")
	private String email;

	@Schema(description = "User's first name", example = "John")
	private String firstName;

	@Schema(description = "User's last name", example = "Doe")
	private String lastName;

	@Schema(description = "User's date of birth", example = "1990-05-15", type = "string", format = "date")
	private LocalDate dateOfBirth;

	@Schema(description = "User's city of residence", example = "Kyiv")
	private String city;

	@Schema(description = "User's phone number", example = "+380501234567")
	private String phoneNumber;
}