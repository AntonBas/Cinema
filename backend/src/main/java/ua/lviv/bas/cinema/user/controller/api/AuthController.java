package ua.lviv.bas.cinema.user.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.config.ratelimit.RateLimit;
import ua.lviv.bas.cinema.config.security.CustomUserDetails;
import ua.lviv.bas.cinema.config.security.JwtBlacklistService;
import ua.lviv.bas.cinema.config.security.JwtCookieService;
import ua.lviv.bas.cinema.config.security.JwtTokenProvider;
import ua.lviv.bas.cinema.config.security.OAuth2ExchangeCodeService;
import ua.lviv.bas.cinema.exception.domain.auth.EmailNotVerifiedException;
import ua.lviv.bas.cinema.exception.domain.auth.InvalidTokenException;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.dto.request.OAuth2ExchangeRequest;
import ua.lviv.bas.cinema.user.dto.request.ResendVerificationRequest;
import ua.lviv.bas.cinema.user.dto.request.UserLoginRequest;
import ua.lviv.bas.cinema.user.dto.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.user.dto.response.AuthResponse;
import ua.lviv.bas.cinema.user.dto.response.ResendVerificationResponse;
import ua.lviv.bas.cinema.user.dto.response.UserResponse;
import ua.lviv.bas.cinema.user.mapper.UserMapper;
import ua.lviv.bas.cinema.user.service.UserPasswordResetService;
import ua.lviv.bas.cinema.user.service.UserService;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Authentication", description = "User authentication and authorization endpoints")
public class AuthController {

    private final UserService userService;
    private final UserPasswordResetService passwordResetService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final JwtCookieService jwtCookieService;
    private final JwtBlacklistService jwtBlacklistService;
    private final OAuth2ExchangeCodeService oAuth2ExchangeCodeService;

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

    @RateLimit(value = 5, duration = 60)
    @PostMapping("/login")
    @Operation(summary = "User login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password, or account blocked"),
            @ApiResponse(responseCode = "403", description = "Email address not confirmed yet")
    })
    @SecurityRequirements()
    public AuthResponse login(@Valid @RequestBody UserLoginRequest request, HttpServletResponse response) {
        log.info("POST /api/auth/login - email: {}", request.email());

        var authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        var userDetails = (CustomUserDetails) authentication.getPrincipal();
        if (!userDetails.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        var token = jwtTokenProvider.generateToken(authentication);
        jwtCookieService.addTokenCookie(response, token);

        var userResponse = userService.getUserResponse(userDetails.getUserId());

        return new AuthResponse(userResponse);
    }

    @RateLimit(value = 10, duration = 60)
    @PostMapping("/oauth2/exchange")
    @Operation(summary = "Exchange OAuth2 one-time code for a session cookie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exchange successful"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired code")
    })
    @SecurityRequirements()
    public AuthResponse oauth2Exchange(@Valid @RequestBody OAuth2ExchangeRequest request, HttpServletResponse response) {
        String email = oAuth2ExchangeCodeService.consume(request.code())
                .orElseThrow(() -> new InvalidTokenException("oauth2-exchange"));
        log.info("POST /api/auth/oauth2/exchange - email: {}", email);

        User user = userService.getUser(email);
        var userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());

        var token = jwtTokenProvider.generateToken(authentication);
        jwtCookieService.addTokenCookie(response, token);

        return new AuthResponse(userMapper.toUserResponse(user));
    }

    @RateLimit(value = 10, duration = 60)
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Log out the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful")
    })
    @SecurityRequirements()
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String token = jwtCookieService.extractToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String jti = jwtTokenProvider.getJtiFromToken(token);
            Duration remaining = Duration.between(Instant.now(), jwtTokenProvider.getExpirationFromToken(token));
            jwtBlacklistService.blacklist(jti, remaining);
            log.info("User logged out, token blacklisted");
        }
        jwtCookieService.clearTokenCookie(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public UserResponse getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new InsufficientAuthenticationException("Not authenticated");
        }
        log.info("GET /api/auth/me - user: {}", userDetails.getUsername());
        return userService.getUserResponse(userDetails.getUserId());
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

    @RateLimit(value = 5, duration = 900)
    @PostMapping("/resend-verification")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Resend email verification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification email sent, or silently ignored if email is unknown"),
            @ApiResponse(responseCode = "400", description = "Email already verified"),
            @ApiResponse(responseCode = "429", description = "Resend requested too soon")
    })
    @SecurityRequirements()
    public ResendVerificationResponse resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        log.info("POST /api/auth/resend-verification - email: {}", request.email());
        int cooldownSeconds = userService.resendVerificationEmail(request.email());
        return new ResendVerificationResponse(cooldownSeconds);
    }

    @RateLimit(value = 15, duration = 60)
    @GetMapping("/resend-verification/status")
    @Operation(summary = "Check remaining resend cooldown without sending an email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cooldown status retrieved")
    })
    @SecurityRequirements()
    public ResendVerificationResponse resendVerificationStatus(@RequestParam @Email @NotBlank String email) {
        int cooldownSeconds = userService.getResendCooldownStatus(email);
        return new ResendVerificationResponse(cooldownSeconds);
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