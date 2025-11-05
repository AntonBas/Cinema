package ua.lviv.bas.cinema.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.mapper.UserMapper;
import ua.lviv.bas.cinema.security.CustomUserDetails;
import ua.lviv.bas.cinema.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final UserMapper userMapper;

	@GetMapping("/profile")
	public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
		log.info("Profile request for user: {}", userDetails.getUsername());

		User user = userService.findByEmail(userDetails.getUsername());
		UserResponse userResponse = userMapper.toDto(user);
		return ResponseEntity.ok(userResponse);
	}

	@PutMapping("/profile")
	public ResponseEntity<UserResponse> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
			@Valid @RequestBody UserUpdateRequest updateRequest) {
		log.info("Updating profile for user ID: {}", userDetails.getUserId());

		UserResponse updatedUser = userService.updateUser(userDetails.getUserId(), updateRequest);
		return ResponseEntity.ok(updatedUser);
	}

	@PatchMapping("/email")
	public ResponseEntity<UserResponse> updateEmail(@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam String newEmail) {
		log.info("Updating email for user ID: {}", userDetails.getUserId());
		UserResponse updatedUser = userService.updateUserEmail(userDetails.getUserId(), newEmail);
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