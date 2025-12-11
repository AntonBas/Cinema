package ua.lviv.bas.cinema.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tickets", ignore = true)
	@Mapping(target = "enabled", constant = "false")
	@Mapping(target = "userRole", constant = "ROLE_USER")
	@Mapping(target = "verificationStatus", constant = "NOT_VERIFIED")
	@Mapping(target = "verifiedAt", ignore = true)
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
	@Mapping(target = "verificationStatus", ignore = true)
	@Mapping(target = "verifiedAt", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "email", ignore = true)
	void updateUserFromDto(UserUpdateRequest dto, @MappingTarget User user);

	@Mapping(target = "ticketsCount", expression = "java(user.getTickets().size())")
	@Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd")
	@Mapping(target = "updatedAt", source = "updatedAt", dateFormat = "yyyy-MM-dd")
	@Mapping(target = "lastActivity", source = "updatedAt", dateFormat = "yyyy-MM-dd")
	AdminUserListResponse toAdminListDto(User user);
}