package ua.lviv.bas.cinema.dto.user.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
	private Long id;
	private String email;
	private String firstName;
	private String lastName;
	private LocalDate dateOfBirth;
	private String city;
	private String phoneNumber;
}