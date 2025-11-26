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
import ua.lviv.bas.cinema.dto.user.request.UserPasswordUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.security.CustomUserDetails;
import ua.lviv.bas.cinema.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping("/profile")
	public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ResponseEntity.ok(userService.getUserProfile(userDetails.getUserId()));
	}

	@PutMapping("/profile")
	public ResponseEntity<UserProfileResponse> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
			@Valid @RequestBody UserUpdateRequest request) {
		return ResponseEntity.ok(userService.updateUser(userDetails.getUserId(), request));
	}

	@PostMapping("/email/change-request")
	public ResponseEntity<?> requestEmailChange(@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam String newEmail) {

		userService.requestEmailChange(userDetails.getUserId(), newEmail);
		return ResponseEntity.ok(Map.of("message", "Confirmation email sent to your new address"));
	}

	@PostMapping("/email/confirm-change")
	public ResponseEntity<UserProfileResponse> confirmEmailChange(@RequestParam String token) {
		return ResponseEntity.ok(userService.confirmEmailChange(token));
	}

	@PatchMapping("/password")
	public ResponseEntity<?> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
			@Valid @RequestBody UserPasswordUpdateRequest request) {
		userService.updateUserPassword(userDetails.getUserId(), request.getCurrentPassword(), request.getNewPassword(),
				request.getPasswordConfirm());
		return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
	}
}