package ua.lviv.bas.cinema.mapper;

import org.springframework.security.crypto.password.PasswordEncoder;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.dto.UserRegistrationDto;

public class UserMapper {

	public static User toEntity(UserRegistrationDto dto, PasswordEncoder encoder) {
		User user = new User();
		user.setEmail(dto.getEmail());
		user.setFirstName(dto.getFirstName());
		user.setLastName(dto.getLastName());
		user.setDateOfBirth(dto.getDateOfBirth());
		user.setCity(dto.getCity());
		user.setPhoneNumber(dto.getPhoneNumber());
		user.setPassword(encoder.encode(dto.getPassword()));
		user.setUserRole(UserRole.ROLE_USER);
		return user;
	}
}
