package ua.lviv.bas.cinema.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;

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

	UserProfileResponse toProfileResponse(User user);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tickets", ignore = true)
	@Mapping(target = "enabled", ignore = true)
	@Mapping(target = "userRole", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "email", ignore = true)
	void updateUserFromDto(UserUpdateRequest dto, @MappingTarget User user);

	default User toEntityWithPassword(UserRegistrationRequest dto, String encodedPassword) {
		User user = toEntity(dto);
		user.setPassword(encodedPassword);
		return user;
	}
}