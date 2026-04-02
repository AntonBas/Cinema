package ua.lviv.bas.cinema.controller.api;

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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.config.ratelimit.RateLimit;
import ua.lviv.bas.cinema.config.security.jwt.JwtTokenProvider;
import ua.lviv.bas.cinema.config.security.user.CustomUserDetails;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.user.request.UserLoginRequest;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.mapper.user.UserMapper;
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
	private final UserMapper userMapper;

	@RateLimit(value = 3, duration = 60, key = "ip")
	@PostMapping("/register")
	@Operation(summary = "Register new user", description = "Create a new user account.")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "User registered successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input data or validation failed"),
			@ApiResponse(responseCode = "409", description = "Email already registered") })
	@SecurityRequirements()
	public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
		log.info("Registration request for email: {}", request.email());
		UserResponse response = userService.registerUser(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@RateLimit(value = 5, duration = 1, key = "ip")
	@PostMapping("/login")
	@Operation(summary = "User login", description = "Authenticate user with email and password.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Login successful"),
			@ApiResponse(responseCode = "401", description = "Invalid email or password"),
			@ApiResponse(responseCode = "403", description = "Account not activated or blocked") })
	@SecurityRequirements()
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
		log.info("Login attempt for email: {}", request.email());

		var authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

		String token = jwtTokenProvider.generateToken(authentication);

		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		UserResponse userResponse = userMapper.toUserResponse(userDetails.getUser());

		return ResponseEntity.ok(new LoginResponse(token, "Bearer", userResponse));
	}

	@GetMapping("/oauth2/success")
	@Operation(summary = "OAuth2 login success", description = "Handle OAuth2 login callback and return user profile")
	@SecurityRequirements()
	public ResponseEntity<LoginResponse> oauth2Success(@Parameter(description = "JWT token") @RequestParam String token,
			@Parameter(description = "User ID") @RequestParam Long userId,
			@Parameter(description = "User email") @RequestParam String email) {

		log.info("OAuth2 login success for email: {}", email);

		User user = userService.getUserById(userId);
		UserResponse userResponse = userMapper.toUserResponse(user);

		return ResponseEntity.ok(new LoginResponse(token, "Bearer", userResponse));
	}

	@GetMapping("/me")
	@Operation(summary = "Get current user profile", description = "Retrieve profile information of the currently authenticated user.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "User not authenticated") })
	public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok(userMapper.toUserResponse(userDetails.getUser()));
	}

	@RateLimit(value = 3, duration = 60, key = "ip")
	@PostMapping("/password/forgot")
	@Operation(summary = "Request password reset", description = "Initiate password reset process.")
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

	@RateLimit(value = 5, duration = 60, key = "ip")
	@PostMapping("/password/reset")
	@Operation(summary = "Reset password", description = "Set new password using reset token.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Password reset successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid or expired token") })
	@SecurityRequirements()
	public ResponseEntity<Void> resetPassword(
			@Parameter(description = "Password reset token", required = true) @RequestParam @NotBlank String token,
			@Parameter(description = "New password", required = true) @RequestParam @NotBlank String newPassword) {
		log.info("Password reset attempt");
		passwordResetService.resetPassword(token, newPassword);
		return ResponseEntity.ok().build();
	}

	@RateLimit(value = 10, duration = 1, key = "ip")
	@GetMapping("/email/check")
	@Operation(summary = "Check email availability", description = "Check if email address is already registered.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Email availability status"),
			@ApiResponse(responseCode = "400", description = "Invalid email format") })
	@SecurityRequirements()
	public ResponseEntity<Boolean> checkEmailExists(
			@Parameter(description = "Email address to check", required = true) @RequestParam @Email @NotBlank String email) {
		boolean exists = userService.emailExists(email);
		return ResponseEntity.ok(exists);
	}

	public record LoginResponse(@Parameter(description = "JWT token for authentication") String token,
			@Parameter(description = "Token type") String tokenType,
			@Parameter(description = "User profile information") UserResponse user) {
	}
}