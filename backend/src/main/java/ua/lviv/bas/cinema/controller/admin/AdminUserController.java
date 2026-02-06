package ua.lviv.bas.cinema.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.user.request.UserFilterRequest;
import ua.lviv.bas.cinema.dto.user.request.UserRoleUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.UserStatusUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.VerificationBirthDateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.service.admin.AdminUserService;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin User Management", description = "Endpoints for managing users (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

	private final AdminUserService adminUserService;

	@GetMapping
	@Operation(summary = "Get users with filters", description = "Retrieve paginated list of users with filtering options. Admin only.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have ADMIN role") })
	public ResponseEntity<PageResponse<AdminUserListResponse>> getUsers(@Valid UserFilterRequest filter,
			@Parameter(hidden = true) Pageable pageable) {

		var page = adminUserService.getUsersForAdmin(filter, pageable);
		return ResponseEntity.ok(PageResponse.from(page));
	}

	@PatchMapping("/{userId}/role")
	@Operation(summary = "Update user role", description = "Change role of a specific user. Admin only.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "User role updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data"),
			@ApiResponse(responseCode = "404", description = "User not found"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have ADMIN role") })
	public ResponseEntity<Void> updateUserRole(
			@Parameter(description = "ID of the user to update", required = true, example = "1") @PathVariable Long userId,
			@Valid @RequestBody UserRoleUpdateRequest request) {

		adminUserService.updateUserRole(userId, request.getUserRole());
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/{userId}/status")
	@Operation(summary = "Update user account status", description = "Enable or disable user account. Admin only.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "User status updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data"),
			@ApiResponse(responseCode = "404", description = "User not found"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have ADMIN role") })
	public ResponseEntity<Void> updateUserStatus(
			@Parameter(description = "ID of the user to update", required = true, example = "1") @PathVariable Long userId,
			@Valid @RequestBody UserStatusUpdateRequest request) {

		adminUserService.updateUserStatus(userId, request.isEnabled());
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/{userId}/verification")
	@Operation(summary = "Update birth date verification", description = "Update user's birth date verification status.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Verification status updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data"),
			@ApiResponse(responseCode = "404", description = "User not found") })
	public ResponseEntity<UserResponse> updateBirthDateVerification(
			@Parameter(description = "ID of the user to update", required = true) @PathVariable Long userId,
			@Valid @RequestBody VerificationBirthDateRequest request) {

		UserResponse response = adminUserService.updateBirthDateVerification(userId, request);
		return ResponseEntity.ok(response);
	}
}