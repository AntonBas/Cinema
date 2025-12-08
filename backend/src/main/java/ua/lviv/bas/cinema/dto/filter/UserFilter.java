package ua.lviv.bas.cinema.dto.filter;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.UserRole;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filter DTO for searching and filtering users")
public class UserFilter {

	@Schema(description = "Search term for filtering users (searches in email, first name, last name)", example = "john.doe", maxLength = 100)
	@Size(max = 100, message = "Search term must not exceed 100 characters")
	@Pattern(regexp = "^[a-zA-Z0-9@.\\-\\s]*$", message = "Invalid characters in search term")
	private String search;

	@Schema(description = "Filter users by role", example = "CUSTOMER", allowableValues = { "ADMIN", "MANAGER",
			"CUSTOMER" })
	private UserRole role;

	@Schema(description = "Filter users by account status (enabled/disabled)", example = "true")
	private Boolean enabled;

	@Schema(description = "Page number for pagination (0-based)", example = "0", defaultValue = "0", minimum = "0")
	@Builder.Default
	@Min(value = 0, message = "Page must be greater than or equal to 0")
	private int page = 0;

	@Schema(description = "Number of items per page", example = "20", defaultValue = "20", minimum = "1", maximum = "100")
	@Builder.Default
	@Min(value = 1, message = "Size must be greater than or equal to 1")
	@Max(value = 100, message = "Size must be less than or equal to 100")
	private int size = 20;

	@Schema(description = "Field to sort by", example = "email", defaultValue = "email", allowableValues = { "email",
			"firstName", "lastName", "createdAt", "id" })
	@Builder.Default
	private String sortBy = "email";

	@Schema(description = "Sort direction", example = "ASC", defaultValue = "ASC", allowableValues = { "ASC", "DESC" })
	@Builder.Default
	private SortDirection sortDirection = SortDirection.ASC;

	@Schema(description = "Sort direction enumeration")
	public enum SortDirection {
		@Schema(description = "Ascending order")
		ASC,

		@Schema(description = "Descending order")
		DESC
	}

	public static final String[] ALLOWED_SORT_FIELDS = { "email", "firstName", "lastName", "createdAt", "id" };

	public boolean isValidSortField() {
		for (String field : ALLOWED_SORT_FIELDS) {
			if (field.equalsIgnoreCase(sortBy)) {
				return true;
			}
		}
		return false;
	}
}