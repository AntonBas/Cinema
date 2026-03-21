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
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.user.request.UserFilterRequest;
import ua.lviv.bas.cinema.dto.user.request.UserRoleUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.UserStatusUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.VerificationBirthDateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.service.admin.AdminUserService;

@Slf4j
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

		log.info("Admin fetching users with filter: {}, pageable: {}", filter, pageable);
		var page = adminUserService.getUsersForAdmin(filter, pageable);
		log.info("Retrieved {} users for admin", page.getTotalElements());

		return ResponseEntity.ok(PageResponse.from(page));
	}

	@PatchMapping("/{userId}/role")
	@Operation(summary = "Update user role", description = "Change role of a specific user. Admin only.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "User role updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data"),
			@ApiResponse(responseCode = "404", description = "User not found"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have ADMIN role or cannot modify own role") })
	public ResponseEntity<AdminUserListResponse> updateUserRole(
			@Parameter(description = "ID of the user to update", required = true, example = "1") @PathVariable Long userId,
			@Valid @RequestBody UserRoleUpdateRequest request) {

		log.info("Admin updating role for user {} to {}", userId, request.userRole());
		AdminUserListResponse response = adminUserService.updateUserRole(userId, request.userRole());
		log.info("User {} role updated successfully to {}", userId, request.userRole());

		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{userId}/status")
	@Operation(summary = "Update user account status", description = "Enable or disable user account. Admin only.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "User status updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data"),
			@ApiResponse(responseCode = "404", description = "User not found"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have ADMIN role or cannot block themselves") })
	public ResponseEntity<AdminUserListResponse> updateUserStatus(
			@Parameter(description = "ID of the user to update", required = true, example = "1") @PathVariable Long userId,
			@Valid @RequestBody UserStatusUpdateRequest request) {

		log.info("Admin updating status for user {} to enabled={}", userId, request.enabled());
		AdminUserListResponse response = adminUserService.updateUserStatus(userId, request.enabled());
		log.info("User {} status updated to enabled={}", userId, request.enabled());

		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{userId}/verification")
	@Operation(summary = "Update birth date verification", description = "Update user's birth date verification status. Admin only.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Verification status updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data"),
			@ApiResponse(responseCode = "404", description = "User not found"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have ADMIN role") })
	public ResponseEntity<AdminUserListResponse> updateBirthDateVerification(
			@Parameter(description = "ID of the user to update", required = true, example = "1") @PathVariable Long userId,
			@Valid @RequestBody VerificationBirthDateRequest request) {

		log.info("Admin updating birth date verification for user {} to {}", userId, request.verificationStatus());

		AdminUserListResponse response = adminUserService.updateBirthDateVerification(userId, request);
		log.info("User {} birth date verification updated to {}", userId, request.verificationStatus());

		return ResponseEntity.ok(response);
	}
}