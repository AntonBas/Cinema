package ua.lviv.bas.cinema.dto.user.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.user.VerificationStatus;

public record UserProfileResponse(@Schema(description = "Unique identifier of the user", example = "1") Long id,

		@Schema(description = "User's email address", example = "john.doe@example.com") String email,

		@Schema(description = "User's first name", example = "John") String firstName,

		@Schema(description = "User's last name", example = "Doe") String lastName,

		@Schema(description = "User's date of birth", example = "1990-05-15") LocalDate dateOfBirth,

		@Schema(description = "User's city of residence", example = "Kyiv") String city,

		@Schema(description = "User's phone number", example = "+380501234567") String phoneNumber,

		@Schema(description = "Birth date verification status", example = "VERIFIED") VerificationStatus verificationStatus) {
}