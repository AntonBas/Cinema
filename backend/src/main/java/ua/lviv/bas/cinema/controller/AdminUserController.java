package ua.lviv.bas.cinema.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.dto.user.request.UserRoleUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.UserStatusUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.service.UserService;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin User Management", description = "Endpoints for managing users (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

	private final UserService userService;

	@GetMapping
	@Operation(summary = "Get all users (paginated)", description = "Retrieve paginated list of all users with filtering options. Admin only.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have ADMIN role") })
	public Page<AdminUserListResponse> getAllUsers(
			@Parameter(description = "Search term (searches in email, first name, last name)") @RequestParam(required = false) String search,

			@Parameter(description = "Filter by user role") @RequestParam(required = false) UserRole role,

			@Parameter(description = "Filter by account status") @RequestParam(required = false) Boolean enabled,

			@Parameter(description = "Pagination parameters") Pageable pageable) {
		return userService.findAllForAdmin(search, role, enabled, pageable);
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

			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Role update request", required = true, content = @Content(schema = @Schema(implementation = UserRoleUpdateRequest.class))) @RequestBody UserRoleUpdateRequest request) {
		userService.updateUserRole(userId, request.getUserRole());
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

			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Status update request", required = true, content = @Content(schema = @Schema(implementation = UserStatusUpdateRequest.class))) @RequestBody UserStatusUpdateRequest request) {
		userService.updateUserStatus(userId, request.isEnabled());
		return ResponseEntity.ok().build();
	}
}