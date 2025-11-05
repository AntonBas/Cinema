package ua.lviv.bas.cinema.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tickets", ignore = true)
	@Mapping(target = "enabled", constant = "false")
	@Mapping(target = "userRole", constant = "ROLE_USER")
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	User toEntity(UserRegistrationRequest dto);

	UserResponse toDto(User user);

	default User toEntityWithPassword(UserRegistrationRequest dto, String encodedPassword) {
		User user = toEntity(dto);
		user.setPassword(encodedPassword);
		return user;
	}
}