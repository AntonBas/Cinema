package ua.lviv.bas.cinema.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.config.JwtTokenProvider;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.user.request.UserLoginRequest;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.security.CustomUserDetails;
import ua.lviv.bas.cinema.service.PasswordResetService;
import ua.lviv.bas.cinema.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final UserService userService;
	private final PasswordResetService passwordResetService;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;

	@PostMapping("/registration")
	public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest request,
			BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return ResponseEntity.badRequest().body(
					Map.of("success", false, "message", "Validation failed", "errors", bindingResult.getAllErrors()));
		}

		if (!request.getPassword().equals(request.getPasswordConfirm())) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Passwords do not match"));
		}

		userService.registerUser(request);
		return ResponseEntity.ok(Map.of("success", true, "message",
				"User registered successfully. Check your email to confirm account."));
	}

	@PostMapping("/login")
	public ResponseEntity<?> loginUser(@Valid @RequestBody UserLoginRequest loginDto) {
		var auth = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));

		String token = jwtTokenProvider.generateToken(auth);
		User user = userService.findByEmail(loginDto.getEmail());

		return ResponseEntity.ok(Map.of("success", true, "token", token, "tokenType", "Bearer", "user",
				userService.getUserById(user.getId())));
	}

	@GetMapping("/me")
	public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok(userService.getUserById(userDetails.getUserId()));
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<?> forgotPassword(@RequestParam String email) {
		passwordResetService.requestPasswordReset(email);
		return ResponseEntity.ok(Map.of("success", true, "message", "Instructions sent to your email."));
	}

	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
		passwordResetService.resetPassword(token, newPassword);
		return ResponseEntity.ok(Map.of("success", true, "message", "Password changed successfully."));
	}

	@GetMapping("/verify-email")
	public ResponseEntity<?> confirmEmail(@RequestParam String token) {
		String message = userService.confirmRegistration(token);
		return ResponseEntity.ok(Map.of("success", true, "message", message));
	}

	@PostMapping("/email/confirm-change")
	public ResponseEntity<?> confirmEmailChange(@RequestParam String token) {
		UserProfileResponse updatedUser = userService.confirmEmailChange(token);
		return ResponseEntity.ok(Map.of("success", true, "id", updatedUser.getId(), "email", updatedUser.getEmail()));
	}

	@GetMapping("/check-email")
	public ResponseEntity<?> checkEmailExists(@RequestParam String email) {
		boolean exists = userService.existsByEmail(email);
		return ResponseEntity.ok(Map.of("exists", exists));
	}
}
