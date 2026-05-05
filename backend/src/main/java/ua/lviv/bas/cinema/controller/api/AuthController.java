package ua.lviv.bas.cinema.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
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
import ua.lviv.bas.cinema.config.security.JwtTokenProvider;
import ua.lviv.bas.cinema.config.security.CustomUserDetails;
import ua.lviv.bas.cinema.dto.user.request.UserLoginRequest;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.response.LoginResponse;
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

    @RateLimit(value = 3, duration = 60)
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    @SecurityRequirements()
    public UserResponse register(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("POST /api/auth/register - email: {}", request.email());
        return userService.register(request);
    }

    @RateLimit(duration = 1)
    @PostMapping("/login")
    @Operation(summary = "User login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    @SecurityRequirements()
    public LoginResponse login(@Valid @RequestBody UserLoginRequest request) {
        log.info("POST /api/auth/login - email: {}", request.email());

        var authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        var token = jwtTokenProvider.generateToken(authentication);
        var userDetails = (CustomUserDetails) authentication.getPrincipal();
        var userResponse = userMapper.toUserResponse(userDetails.getUser());

        return new LoginResponse(token, "Bearer", userResponse);
    }

    @GetMapping("/oauth2/success")
    @Operation(summary = "OAuth2 login success")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OAuth2 login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid token")
    })
    @SecurityRequirements()
    public LoginResponse oauth2Success(@RequestParam String token, @RequestParam Long userId,
                                       @RequestParam String email) {
        log.info("GET /api/auth/oauth2/success - email: {}", email);
        var user = userService.getUser(userId);
        var userResponse = userMapper.toUserResponse(user);
        return new LoginResponse(token, "Bearer", userResponse);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public UserResponse getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new org.springframework.security.core.AuthenticationException("Not authenticated") {
            };
        }
        log.info("GET /api/auth/me - user: {}", userDetails.getUsername());
        return userMapper.toUserResponse(userDetails.getUser());
    }

    @RateLimit(value = 3, duration = 60)
    @PostMapping("/password/forgot")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Request password reset")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reset email sent"),
            @ApiResponse(responseCode = "400", description = "Invalid email format")
    })
    @SecurityRequirements()
    public void forgotPassword(@RequestParam @Email @NotBlank String email) {
        log.info("POST /api/auth/password/forgot - email: {}", email);
        passwordResetService.requestReset(email);
    }

    @RateLimit(duration = 60)
    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Reset password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @SecurityRequirements()
    public void resetPassword(@RequestParam @NotBlank String token, @RequestParam @NotBlank String newPassword) {
        log.info("POST /api/auth/password/reset");
        passwordResetService.reset(token, newPassword);
    }

    @GetMapping("/email/check")
    @Operation(summary = "Check email availability")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email availability checked")
    })
    @SecurityRequirements()
    public boolean checkEmail(@RequestParam @Email @NotBlank String email) {
        log.info("GET /api/auth/email/check - email: {}", email);
        return userService.emailExists(email);
    }
}