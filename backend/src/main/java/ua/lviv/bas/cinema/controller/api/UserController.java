package ua.lviv.bas.cinema.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ua.lviv.bas.cinema.config.ratelimit.RateLimit;
import ua.lviv.bas.cinema.config.security.CustomUserDetails;
import ua.lviv.bas.cinema.dto.user.request.UserEmailChangeRequest;
import ua.lviv.bas.cinema.dto.user.request.UserPasswordUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.service.user.UserService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile Management", description = "Endpoints for managing user profiles")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @RateLimit(value = 30, duration = 1, key = "user")
    @GetMapping("/profile")
    @Operation(summary = "Get user profile")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")})
    public UserProfileResponse getProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("GET /api/users/profile - user: {}", userDetails.getUserId());
        return userService.getProfile(userDetails.getUserId());
    }

    @RateLimit(duration = 5, key = "user")
    @PutMapping("/profile")
    @Operation(summary = "Update user profile")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")})
    public UserProfileResponse updateProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("PUT /api/users/profile - user: {}", userDetails.getUserId());
        return userService.update(userDetails.getUserId(), request);
    }

    @RateLimit(value = 3, duration = 60, key = "user")
    @PostMapping("/email/change-request")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Request email change")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Email change request sent"),
            @ApiResponse(responseCode = "400", description = "Invalid email format")})
    public Map<String, String> requestEmailChange(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserEmailChangeRequest request) {
        log.info("POST /api/users/email/change-request - user: {}", userDetails.getUserId());
        userService.requestEmailChange(userDetails.getUserId(), request.password(), request.newEmail());
        return Map.of("message", "Confirmation email sent to your new address");
    }

    @RateLimit(key = "user")
    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update password")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Password updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")})
    public Map<String, String> updatePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserPasswordUpdateRequest request) {
        log.info("PATCH /api/users/password - user: {}", userDetails.getUserId());
        userService.updatePassword(userDetails.getUserId(), request);
        return Map.of("message", "Password updated successfully");
    }
}