package ua.lviv.bas.cinema.user.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import ua.lviv.bas.cinema.config.security.CustomUserDetails;
import ua.lviv.bas.cinema.config.security.JwtBlacklistService;
import ua.lviv.bas.cinema.config.security.JwtCookieService;
import ua.lviv.bas.cinema.config.security.JwtTokenProvider;
import ua.lviv.bas.cinema.config.security.OAuth2ExchangeCodeService;
import ua.lviv.bas.cinema.exception.domain.auth.EmailNotVerifiedException;
import ua.lviv.bas.cinema.exception.domain.auth.InvalidTokenException;
import ua.lviv.bas.cinema.user.dto.request.UserLoginRequest;
import ua.lviv.bas.cinema.user.dto.response.AuthResponse;
import ua.lviv.bas.cinema.user.mapper.UserMapper;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtCookieService jwtCookieService;
    private final JwtBlacklistService jwtBlacklistService;
    private final OAuth2ExchangeCodeService oAuth2ExchangeCodeService;

    public AuthResponse login(UserLoginRequest request, HttpServletResponse response) {
        var authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        var userDetails = (CustomUserDetails) authentication.getPrincipal();
        if (!userDetails.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        jwtCookieService.addTokenCookie(response, jwtTokenProvider.generateToken(userDetails));
        return new AuthResponse(userService.getUserResponse(userDetails.getUserId()));
    }

    public AuthResponse exchangeOAuth2Code(String code, HttpServletResponse response) {
        String email = oAuth2ExchangeCodeService.consume(code)
                .orElseThrow(() -> new InvalidTokenException("oauth2-exchange"));

        var user = userService.getUser(email);
        jwtCookieService.addTokenCookie(response, jwtTokenProvider.generateToken(new CustomUserDetails(user)));
        return new AuthResponse(userMapper.toUserResponse(user));
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String token = jwtCookieService.extractToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Duration remaining = Duration.between(Instant.now(), jwtTokenProvider.getExpirationFromToken(token));
            jwtBlacklistService.blacklist(jwtTokenProvider.getJtiFromToken(token), remaining);
            log.info("User logged out, token blacklisted");
        }
        jwtCookieService.clearTokenCookie(response);
    }
}
