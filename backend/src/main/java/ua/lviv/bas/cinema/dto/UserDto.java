package ua.lviv.bas.cinema.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import ua.lviv.bas.cinema.domain.enums.UserRole;

@Data
@Builder
public class UserDto {
	private Long id;
	private String email;
	private String firstName;
	private String lastName;
	private LocalDate dateOfBirth;
	private String city;
	private String phoneNumber;
	private UserRole userRole;
	private boolean enabled;
	private LocalDateTime createdAt;
}
