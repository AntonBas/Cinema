package ua.lviv.bas.cinema.dto.filter;

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
public class UserFilter {

	@Size(max = 100, message = "Search term must not exceed 100 characters")
	@Pattern(regexp = "^[a-zA-Z0-9@.\\-\\s]*$", message = "Invalid characters in search term")
	private String search;

	private UserRole role;
	private Boolean enabled;

	@Builder.Default
	@Min(value = 0, message = "Page must be greater than or equal to 0")
	private int page = 0;

	@Builder.Default
	@Min(value = 1, message = "Size must be greater than or equal to 1")
	@Max(value = 100, message = "Size must be less than or equal to 100")
	private int size = 20;

	@Builder.Default
	private String sortBy = "email";

	@Builder.Default
	private SortDirection sortDirection = SortDirection.ASC;

	public enum SortDirection {
		ASC, DESC
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