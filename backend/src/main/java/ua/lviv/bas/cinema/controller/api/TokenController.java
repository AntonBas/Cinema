package ua.lviv.bas.cinema.controller.api;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.mapper.UserMapper;
import ua.lviv.bas.cinema.service.notification.EmailTokenService;

@Slf4j
@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
@Tag(name = "Token Management", description = "Endpoints for managing email tokens")
public class TokenController {

	private final EmailTokenService emailTokenService;
	private final UserMapper userMapper;

	@PostMapping("/email/verify")
	@Operation(summary = "Verify email address", description = "Confirm user's email address using verification token.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Email verified successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid or expired token") })
	@SecurityRequirements()
	public ResponseEntity<Map<String, String>> verifyEmail(
			@Parameter(description = "Email verification token", required = true) @RequestParam @NotBlank String token) {

		log.info("Email verification attempt");
		String message = emailTokenService.confirmEmail(token);
		return ResponseEntity.ok(Map.of("message", message));
	}

	@PostMapping("/email/change/confirm")
	@Operation(summary = "Confirm email change", description = "Confirm email change using token.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Email changed successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid or expired token") })
	@SecurityRequirements()
	public ResponseEntity<UserResponse> confirmEmailChange(
			@Parameter(description = "Email change token", required = true) @RequestParam @NotBlank String token) {

		log.info("Email change confirmation attempt");
		User user = emailTokenService.confirmEmailChange(token);
		return ResponseEntity.ok(userMapper.toUserResponse(user));
	}
}