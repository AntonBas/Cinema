package ua.lviv.bas.cinema.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UserUpdateRequest(
        @Schema(description = "User's first name", example = "John", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 50)
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        @NotBlank(message = "First name is required")
        String firstName,

        @Schema(description = "User's last name", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 50)
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        @NotBlank(message = "Last name is required")
        String lastName,

        @Schema(description = "User's date of birth", example = "1990-05-15", requiredMode = Schema.RequiredMode.REQUIRED, type = "string", format = "date")
        @Past(message = "Date of birth must be in the past")
        @NotNull(message = "Date of birth is required")
        LocalDate dateOfBirth,

        @Schema(description = "User's city of residence", example = "Lviv", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 50)
        @Size(min = 2, max = 50, message = "City name must be between 2 and 50 characters")
        @NotBlank(message = "City is required")
        String city,

        @Schema(description = "User's phone number", example = "+380234567891", requiredMode = Schema.RequiredMode.REQUIRED, pattern = "^\\+?[0-9]{10,15}$")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
        @NotBlank(message = "Phone Number is required")
        String phoneNumber
) {
}