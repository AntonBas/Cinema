package ua.lviv.bas.cinema.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

	@PostMapping("/register")
	public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
		log.info("Registration request for email: {}", request.getEmail());
		UserResponse userResponse = userService.registerUser(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
		log.info("Login attempt for email: {}", request.getEmail());

		var authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

		String token = jwtTokenProvider.generateToken(authentication);
		User user = userService.findByEmail(request.getEmail());
		UserResponse userResponse = userService.getUserById(user.getId());

		return ResponseEntity.ok(new LoginResponse(token, "Bearer", userResponse));
	}

	@GetMapping("/me")
	public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ResponseEntity.ok(userService.getUserById(userDetails.getUserId()));
	}

	@PostMapping("/password/forgot")
	public ResponseEntity<Void> forgotPassword(@RequestParam @Email @NotBlank String email) {
		log.info("Password reset request for email: {}", email);
		passwordResetService.requestPasswordReset(email);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/password/reset")
	public ResponseEntity<Void> resetPassword(@RequestParam @NotBlank String token,
			@RequestParam @NotBlank String newPassword) {
		log.info("Password reset attempt");
		passwordResetService.resetPassword(token, newPassword);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/email/verify")
	public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam @NotBlank String token) {
		log.info("Email verification attempt");
		String message = userService.confirmRegistration(token);
		return ResponseEntity.ok(Map.of("message", message));
	}

	@PostMapping("/email/change/confirm")
	public ResponseEntity<UserProfileResponse> confirmEmailChange(@RequestParam @NotBlank String token) {
		log.info("Email change confirmation attempt");
		UserProfileResponse updatedUser = userService.confirmEmailChange(token);
		return ResponseEntity.ok(updatedUser);
	}

	@GetMapping("/email/check")
	public ResponseEntity<Boolean> checkEmailExists(@RequestParam @Email @NotBlank String email) {
		boolean exists = userService.existsByEmail(email);
		return ResponseEntity.ok(exists);
	}

	public record LoginResponse(String token, String tokenType, UserResponse user) {
	}
}