package ua.lviv.bas.cinema.controller.api;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import ua.lviv.bas.cinema.config.ratelimit.RateLimit;
import ua.lviv.bas.cinema.config.security.user.CustomUserDetails;
import ua.lviv.bas.cinema.dto.user.request.UserEmailChangeRequest;
import ua.lviv.bas.cinema.dto.user.request.UserPasswordUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.service.user.UserService;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile Management", description = "Endpoints for managing user profiles")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

	private final UserService userService;

	@GetMapping("/profile")
	@Operation(summary = "Get user profile", description = "Retrieve the authenticated user's profile information.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "404", description = "User not found") })
	public ResponseEntity<UserProfileResponse> getProfile(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

		log.debug("GET /api/users/profile - userDetails: {}", userDetails);

		if (userDetails == null) {
			log.error("UserDetails is null - user not authenticated");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		log.info("GET /api/users/profile - user ID: {}", userDetails.getUserId());
		return ResponseEntity.ok(userService.getUserProfile(userDetails.getUserId()));
	}

	@RateLimit(value = 10, duration = 5, key = "user")
	@PutMapping("/profile")
	@Operation(summary = "Update user profile", description = "Update the authenticated user's profile information.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "404", description = "User not found") })
	public ResponseEntity<UserProfileResponse> updateProfile(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
			@Valid @RequestBody UserUpdateRequest request) {

		log.info("PUT /api/users/profile - user ID: {}", userDetails.getUserId());
		return ResponseEntity.ok(userService.updateUser(userDetails.getUserId(), request));
	}

	@RateLimit(value = 3, duration = 60, key = "user")
	@PostMapping("/email/change-request")
	@Operation(summary = "Request email change", description = "Initiate email change process.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Email change request sent successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid email format or email already in use"),
			@ApiResponse(responseCode = "401", description = "User not authenticated or current password is incorrect"),
			@ApiResponse(responseCode = "404", description = "User not found") })
	public ResponseEntity<Map<String, String>> requestEmailChange(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
			@Valid @RequestBody UserEmailChangeRequest request) {

		log.info("POST /api/users/email/change-request - user ID: {}", userDetails.getUserId());
		userService.requestEmailChange(userDetails.getUserId(), request.password(), request.newEmail());
		return ResponseEntity.ok(Map.of("message", "Confirmation email sent to your new address"));
	}

	@RateLimit(value = 5, duration = 15, key = "user")
	@PatchMapping("/password")
	@Operation(summary = "Update password", description = "Change the authenticated user's password.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Password updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data or passwords do not match"),
			@ApiResponse(responseCode = "401", description = "User not authenticated or current password is incorrect"),
			@ApiResponse(responseCode = "404", description = "User not found") })
	public ResponseEntity<Map<String, String>> updatePassword(
			@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
			@Valid @RequestBody UserPasswordUpdateRequest request) {

		log.info("PATCH /api/users/password - user ID: {}", userDetails.getUserId());
		userService.updateUserPassword(userDetails.getUserId(), request);
		return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
	}
}