package ua.lviv.bas.cinema.mapper.user;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.repository.user.projection.AdminUserProjection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
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
	User toUser(UserRegistrationRequest request);

	UserResponse toUserResponse(User user);

	UserProfileResponse toUserProfileResponse(User user);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "email", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "tickets", ignore = true)
	@Mapping(target = "bonusCard", ignore = true)
	@Mapping(target = "bookings", ignore = true)
	@Mapping(target = "redeemedPromotions", ignore = true)
	@Mapping(target = "enabled", ignore = true)
	@Mapping(target = "userRole", ignore = true)
	@Mapping(target = "verificationStatus", ignore = true)
	@Mapping(target = "verifiedAt", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "createdDate", ignore = true)
	@Mapping(target = "lastModifiedBy", ignore = true)
	@Mapping(target = "lastModifiedDate", ignore = true)
	void updateUserFromRequest(UserUpdateRequest request, @MappingTarget User user);

	AdminUserListResponse toAdminUserListResponse(AdminUserProjection projection);

	@Mapping(target = "ticketsCount", expression = "java(user.getTickets() != null ? (long) user.getTickets().size() : 0L)")
	@Mapping(target = "lastActivity", ignore = true)
	AdminUserListResponse toAdminUserListResponse(User user);
}