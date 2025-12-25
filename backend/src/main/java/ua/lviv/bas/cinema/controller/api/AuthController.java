package ua.lviv.bas.cinema.controller.api;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import ua.lviv.bas.cinema.service.user.UserPasswordResetService;
import ua.lviv.bas.cinema.service.user.UserService;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and authorization endpoints")
public class AuthController {

	private final UserService userService;
	private final UserPasswordResetService passwordResetService;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;

	@PostMapping("/register")
	@Operation(summary = "Register new user", description = "Create a new user account. Returns the created user profile.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input data or validation failed"),
			@ApiResponse(responseCode = "409", description = "Email already registered") })
	@SecurityRequirements()
	public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
		log.info("Registration request for email: {}", request.getEmail());
		UserResponse userResponse = userService.registerUser(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
	}

	@PostMapping("/login")
	@Operation(summary = "User login", description = "Authenticate user with email and password. Returns JWT token for authorization.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
			@ApiResponse(responseCode = "401", description = "Invalid email or password"),
			@ApiResponse(responseCode = "403", description = "Account not activated or blocked") })
	@SecurityRequirements()
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
	@Operation(summary = "Get current user profile", description = "Retrieve profile information of the currently authenticated user.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User profile retrieved successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
			@ApiResponse(responseCode = "401", description = "User not authenticated") })
	public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		return ResponseEntity.ok(userService.getUserById(userDetails.getUserId()));
	}

	@PostMapping("/password/forgot")
	@Operation(summary = "Request password reset", description = "Initiate password reset process. Sends reset link to user's email.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Reset email sent if account exists and email is verified"),
			@ApiResponse(responseCode = "400", description = "Invalid email format"),
			@ApiResponse(responseCode = "403", description = "Email not verified") })
	@SecurityRequirements()
	public ResponseEntity<Void> forgotPassword(
			@Parameter(description = "User's email address", required = true, example = "user@example.com") @RequestParam @Email @NotBlank String email) {
		log.info("Password reset request for email: {}", email);
		passwordResetService.requestPasswordReset(email);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/password/reset")
	@Operation(summary = "Reset password", description = "Set new password using reset token.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Password reset successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid or expired token") })
	@SecurityRequirements()
	public ResponseEntity<Void> resetPassword(
			@Parameter(description = "Password reset token", required = true, example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") @RequestParam @NotBlank String token,
			@Parameter(description = "New password", required = true, example = "NewSecurePassword123!") @RequestParam @NotBlank String newPassword) {
		log.info("Password reset attempt");
		passwordResetService.resetPassword(token, newPassword);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/email/verify")
	@Operation(summary = "Verify email address", description = "Confirm user's email address using verification token.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Email verified successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid or expired token") })
	@SecurityRequirements()
	public ResponseEntity<Map<String, String>> verifyEmail(
			@Parameter(description = "Email verification token", required = true, example = "abc123def456") @RequestParam @NotBlank String token) {
		log.info("Email verification attempt");
		String message = userService.confirmRegistration(token);
		return ResponseEntity.ok(Map.of("message", message));
	}

	@PostMapping("/email/change/confirm")
	@Operation(summary = "Confirm email change", description = "Confirm email address change using confirmation token.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Email changed successfully", content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid or expired token") })
	@SecurityRequirements()
	public ResponseEntity<UserProfileResponse> confirmEmailChange(
			@Parameter(description = "Email change confirmation token", required = true, example = "xyz789uvw456") @RequestParam @NotBlank String token) {
		log.info("Email change confirmation attempt");
		UserProfileResponse updatedUser = userService.confirmEmailChange(token);
		return ResponseEntity.ok(updatedUser);
	}

	@GetMapping("/email/check")
	@Operation(summary = "Check email availability", description = "Check if email address is already registered in the system.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Email availability status", content = @Content(schema = @Schema(implementation = Boolean.class))),
			@ApiResponse(responseCode = "400", description = "Invalid email format") })
	@SecurityRequirements()
	public ResponseEntity<Boolean> checkEmailExists(
			@Parameter(description = "Email address to check", required = true, example = "user@example.com") @RequestParam @Email @NotBlank String email) {
		boolean exists = userService.existsByEmail(email);
		return ResponseEntity.ok(exists);
	}

	@Schema(description = "Login response containing JWT token and user information")
	public record LoginResponse(
			@Schema(description = "JWT token for authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String token,

			@Schema(description = "Token type", example = "Bearer") String tokenType,

			@Schema(description = "User profile information") UserResponse user) {
	}
}