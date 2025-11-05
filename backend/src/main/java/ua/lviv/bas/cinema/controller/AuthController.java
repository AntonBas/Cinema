package ua.lviv.bas.cinema.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ua.lviv.bas.cinema.config.JwtTokenProvider;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.user.UserDto;
import ua.lviv.bas.cinema.dto.user.UserLoginDto;
import ua.lviv.bas.cinema.dto.user.UserRegistrationDto;
import ua.lviv.bas.cinema.security.CustomUserDetails;
import ua.lviv.bas.cinema.service.EmailTokenGeneratorService;
import ua.lviv.bas.cinema.service.EmailTokenService;
import ua.lviv.bas.cinema.service.PasswordResetService;
import ua.lviv.bas.cinema.service.UserService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final UserService userService;
	private final EmailTokenGeneratorService tokenGeneratorService;
	private final EmailTokenService emailTokenService;
	private final PasswordResetService passwordResetService;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;

	@PostMapping("/registration")
	public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto userDto,
			BindingResult bindingResult) {
		log.info("Registration attempt for email: {}", userDto.getEmail());

		if (bindingResult.hasErrors()) {
			log.warn("Validation errors for registration: {}", bindingResult.getAllErrors());
			return ResponseEntity.badRequest()
					.body(createErrorResponse("Validation failed", bindingResult.getAllErrors()));
		}

		if (!userDto.getPassword().equals(userDto.getPasswordConfirm())) {
			log.warn("Password mismatch for email: {}", userDto.getEmail());
			return ResponseEntity.badRequest().body(createErrorResponse("Passwords do not match"));
		}

		try {
			userService.registerUser(userDto);
			tokenGeneratorService.generateVerificationToken(userDto.getEmail());
			log.info("User registered successfully: {}", userDto.getEmail());
			return ResponseEntity.ok().body(createSuccessResponse("Check your email to confirm account"));
		} catch (RuntimeException e) {
			log.error("Registration failed for email {}: {}", userDto.getEmail(), e.getMessage());
			return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> loginUser(@Valid @RequestBody UserLoginDto loginDto) {
		log.info("Login attempt for email: {}", loginDto.getEmail());

		try {
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));

			String token = jwtTokenProvider.generateToken(authentication);
			User user = userService.findByEmail(loginDto.getEmail());

			Map<String, Object> response = createLoginResponse(token, user);

			response.put("token", token);
			response.put("tokenType", "Bearer");

			log.info("Login successful for email: {}", loginDto.getEmail());
			return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + token).body(response);

		} catch (BadCredentialsException e) {
			log.warn("Invalid credentials for email: {}", loginDto.getEmail());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(createErrorResponse("Invalid email or password"));
		} catch (DisabledException e) {
			log.warn("Account not verified for email: {}", loginDto.getEmail());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(createErrorResponse("Account is not verified. Please check your email."));
		}
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<?> forgotPassword(@RequestParam String email) {
		log.info("Password reset request for email: {}", email);

		try {
			passwordResetService.requestPasswordReset(email);
			log.info("Password reset instructions sent to: {}", email);
			return ResponseEntity.ok().body(createSuccessResponse("Instructions have been sent to your email address"));
		} catch (RuntimeException e) {
			log.warn("Password reset request failed for {}: {}", email, e.getMessage());
			return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
		}
	}

	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
		log.info("Password reset attempt with token");

		try {
			passwordResetService.resetPassword(token, newPassword);
			log.info("Password reset successful");
			return ResponseEntity.ok().body(createSuccessResponse("Password has been successfully changed"));
		} catch (RuntimeException e) {
			log.warn("Password reset failed: {}", e.getMessage());
			return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
		}
	}

	@GetMapping("/verify-email")
	public ResponseEntity<?> confirmEmailToken(@RequestParam("token") String token) {
		log.info("Email verification attempt with token");

		try {
			emailTokenService.confirmEmail(token);
			log.info("Email verified successfully");
			return ResponseEntity.ok(createSuccessResponse("Email successfully verified! You can now log in."));
		} catch (RuntimeException e) {
			log.warn("Email verification failed: {}", e.getMessage());
			return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
		}
	}

	@GetMapping("/check-email")
	public ResponseEntity<?> checkEmailExists(@RequestParam String email) {
		log.debug("Checking email existence: {}", email);

		boolean exists = userService.existsByEmail(email);
		Map<String, Boolean> response = new HashMap<>();
		response.put("exists", exists);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/profile")
	public ResponseEntity<?> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
		if (userDetails == null) {
			log.warn("Unauthorized profile access attempt");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(createErrorResponse("User not authenticated"));
		}

		try {
			String email = userDetails.getUsername();
			log.info("Profile request for user: {}", email);

			User user = userService.findByEmail(email);

			UserDto userDto = buildUserDto(user);

			log.info("Profile retrieved successfully for user: {}", email);
			return ResponseEntity.ok(userDto);

		} catch (EntityNotFoundException e) {
			log.warn("User profile not found: {}", userDetails.getUsername());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse("User not found"));
		} catch (Exception e) {
			log.error("Internal server error in getProfile for user {}: {}", userDetails.getUsername(), e.getMessage(),
					e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse("Internal server error"));
		}
	}

	private Map<String, Object> createLoginResponse(String token, User user) {
		Map<String, Object> response = new HashMap<>();
		response.put("message", "Login successful");
		response.put("token", token);
		response.put("user", UserDto.builder().id(user.getId()).email(user.getEmail()).firstName(user.getFirstName())
				.lastName(user.getLastName()).userRole(user.getUserRole()).build());
		return response;
	}

	private UserDto buildUserDto(User user) {
		return UserDto.builder().id(user.getId()).email(user.getEmail()).firstName(user.getFirstName())
				.lastName(user.getLastName()).dateOfBirth(user.getDateOfBirth()).city(user.getCity())
				.phoneNumber(user.getPhoneNumber()).userRole(user.getUserRole()).enabled(user.isEnabled()).build();
	}

	private Map<String, Object> createSuccessResponse(String message) {
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", message);
		return response;
	}

	private Map<String, Object> createErrorResponse(String message) {
		Map<String, Object> response = new HashMap<>();
		response.put("success", false);
		response.put("message", message);
		return response;
	}

	private Map<String, Object> createErrorResponse(String message, Object errors) {
		Map<String, Object> response = createErrorResponse(message);
		response.put("errors", errors);
		return response;
	}
}