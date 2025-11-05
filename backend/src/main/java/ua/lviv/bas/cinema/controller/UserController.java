package ua.lviv.bas.cinema.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.security.CustomUserDetails;
import ua.lviv.bas.cinema.service.EmailTokenGeneratorService;
import ua.lviv.bas.cinema.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final EmailTokenGeneratorService emailTokenGeneratorService;

	@GetMapping("/profile")
	public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
		log.info("Profile request for user: {}", userDetails.getUsername());

		UserProfileResponse userResponse = userService.getUserProfileById(userDetails.getUserId());
		return ResponseEntity.ok(userResponse);
	}

	@PutMapping("/profile")
	public ResponseEntity<UserProfileResponse> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
			@Valid @RequestBody UserUpdateRequest updateRequest) {
		log.info("Updating profile for user ID: {}", userDetails.getUserId());

		UserProfileResponse updatedUser = userService.updateUser(userDetails.getUserId(), updateRequest);
		return ResponseEntity.ok(updatedUser);
	}

	@PostMapping("/email/change-request")
	public ResponseEntity<?> requestEmailChange(@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam String newEmail) {
		log.info("Requesting email change for user ID: {} to {}", userDetails.getUserId(), newEmail);

		userService.requestEmailChange(userDetails.getUserId(), newEmail);

		var user = userService.findById(userDetails.getUserId());
		emailTokenGeneratorService.generateEmailChangeToken(user.getEmail(), newEmail);

		return ResponseEntity.ok(Map.of("message", "Confirmation email sent to your new email address"));
	}

	@PostMapping("/email/confirm-change")
	public ResponseEntity<UserProfileResponse> confirmEmailChange(@RequestParam String token) {
		log.info("Confirming email change with token");

		UserProfileResponse updatedUser = userService.confirmEmailChange(token);
		return ResponseEntity.ok(updatedUser);
	}

	@PatchMapping("/password")
	public ResponseEntity<?> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam String newPassword) {

		log.info("Updating password for user ID: {}", userDetails.getUserId());
		userService.updateUserPassword(userDetails.getUserId(), newPassword);
		return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
	}
}