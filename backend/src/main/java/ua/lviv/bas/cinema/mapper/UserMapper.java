package ua.lviv.bas.cinema.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.projection.user.AdminUserProjection;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tickets", ignore = true)
	@Mapping(target = "bonusCard", ignore = true)
	@Mapping(target = "bookings", ignore = true)
	@Mapping(target = "redeemedPromotions", ignore = true)
	@Mapping(target = "enabled", constant = "false")
	@Mapping(target = "userRole", constant = "ROLE_USER")
	@Mapping(target = "verificationStatus", constant = "NOT_VERIFIED")
	@Mapping(target = "verifiedAt", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	User toUser(UserRegistrationRequest dto);

	UserResponse toUserResponse(User user);

	UserProfileResponse toUserProfileResponse(User user);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tickets", ignore = true)
	@Mapping(target = "bonusCard", ignore = true)
	@Mapping(target = "bookings", ignore = true)
	@Mapping(target = "redeemedPromotions", ignore = true)
	@Mapping(target = "enabled", ignore = true)
	@Mapping(target = "userRole", ignore = true)
	@Mapping(target = "verificationStatus", ignore = true)
	@Mapping(target = "verifiedAt", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "email", ignore = true)
	void updateUserFromRequest(UserUpdateRequest dto, @MappingTarget User user);

	@Mapping(target = "id", source = "id")
	@Mapping(target = "email", source = "email")
	@Mapping(target = "firstName", source = "firstName")
	@Mapping(target = "lastName", source = "lastName")
	@Mapping(target = "userRole", source = "userRole")
	@Mapping(target = "enabled", source = "enabled")
	@Mapping(target = "verificationStatus", source = "verificationStatus")
	@Mapping(target = "verifiedAt", source = "verifiedAt")
	@Mapping(target = "ticketsCount", source = "ticketsCount")
	@Mapping(target = "lastActivity", source = "lastActivity")
	AdminUserListResponse toAdminUserListResponse(AdminUserProjection projection);

	@Mapping(target = "id", source = "id")
	@Mapping(target = "email", source = "email")
	@Mapping(target = "firstName", source = "firstName")
	@Mapping(target = "lastName", source = "lastName")
	@Mapping(target = "userRole", source = "userRole")
	@Mapping(target = "enabled", source = "enabled")
	@Mapping(target = "verificationStatus", source = "verificationStatus")
	@Mapping(target = "verifiedAt", source = "verifiedAt")
	@Mapping(target = "ticketsCount", expression = "java(user.getTickets() != null ? (long) user.getTickets().size() : 0L)")
	@Mapping(target = "lastActivity", source = "updatedAt")
	AdminUserListResponse toAdminUserListResponse(User user);
}