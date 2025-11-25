package ua.lviv.bas.cinema.dto.user.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.UserRole;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserListResponse {

	private Long id;
	private String email;
	private String firstName;
	private String lastName;
	private UserRole userRole;
	private boolean enabled;
	private LocalDate createdAt;
	private LocalDate updatedAt;

	private int ticketsCount;
	private LocalDate lastActivity;
}
