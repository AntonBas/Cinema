package ua.lviv.bas.cinema.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.UserDto;
import ua.lviv.bas.cinema.dto.UserRegistrationDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tickets", ignore = true)
	@Mapping(target = "enabled", constant = "false")
	@Mapping(target = "userRole", constant = "ROLE_USER")
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	User toEntity(UserRegistrationDto dto);

	UserDto toDto(User user);

	default User toEntityWithPassword(UserRegistrationDto dto, String encodedPassword) {
		User user = toEntity(dto);
		user.setPassword(encodedPassword);
		return user;
	}
}