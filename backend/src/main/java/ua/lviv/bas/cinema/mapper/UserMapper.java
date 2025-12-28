package ua.lviv.bas.cinema.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tickets", ignore = true)
	@Mapping(target = "bonusCard", ignore = true)
	@Mapping(target = "discounts", ignore = true)
	@Mapping(target = "bookings", ignore = true)
	@Mapping(target = "enabled", constant = "false")
	@Mapping(target = "userRole", constant = "ROLE_USER")
	@Mapping(target = "verificationStatus", constant = "NOT_VERIFIED")
	@Mapping(target = "verifiedAt", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	User toEntity(UserRegistrationRequest dto);

	@Mapping(target = "verificationStatus", source = "verificationStatus")
	@Mapping(target = "userRole", source = "userRole")
	UserResponse toDto(User user);

	@Mapping(target = "verificationStatus", source = "verificationStatus")
	UserProfileResponse toProfileResponse(User user);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tickets", ignore = true)
	@Mapping(target = "bonusCard", ignore = true)
	@Mapping(target = "discounts", ignore = true)
	@Mapping(target = "bookings", ignore = true)
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
	@Mapping(target = "lastActivity", source = "updatedAt")
	AdminUserListResponse toAdminListDto(User user);
}