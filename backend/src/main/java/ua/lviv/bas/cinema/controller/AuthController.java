package ua.lviv.bas.cinema.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
		try {
			Authentication auth = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));

			String token = jwtTokenProvider.generateToken(auth);
			User user = userService.findByEmail(loginDto.getEmail());

			return ResponseEntity
					.ok(Map.of("token", token, "tokenType", "Bearer", "user", userService.getUserById(user.getId())));
		} catch (BadCredentialsException e) {
			return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));
		} catch (DisabledException e) {
			return ResponseEntity.status(401).body(Map.of("message", "Account not verified. Check your email."));
		}
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<?> forgotPassword(@RequestParam String email) {
		passwordResetService.requestPasswordReset(email);
		return ResponseEntity.ok(Map.of("message", "Instructions sent to your email."));
	}

	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
		passwordResetService.resetPassword(token, newPassword);
		return ResponseEntity.ok(Map.of("message", "Password changed successfully."));
	}

	@GetMapping("/verify-email")
	public ResponseEntity<?> confirmEmail(@RequestParam String token) {
		String result = userService.confirmEmailChange(token).getEmail();
		return ResponseEntity.ok(Map.of("message", "Email verified successfully for: " + result));
	}
}