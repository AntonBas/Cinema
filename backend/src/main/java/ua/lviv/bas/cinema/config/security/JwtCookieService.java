package ua.lviv.bas.cinema.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class JwtCookieService {

    public static final String COOKIE_NAME = "jwt";

    private final long expirationMs;
    private final boolean secure;
    private final String sameSite;

    public JwtCookieService(@Value("${app.jwt.expiration}") long expirationMs,
                             @Value("${app.jwt.cookie-secure:false}") boolean secure,
                             @Value("${app.jwt.cookie-same-site:Lax}") String sameSite) {
        this.expirationMs = expirationMs;
        this.secure = secure;
        this.sameSite = sameSite;
    }

    public void addTokenCookie(HttpServletResponse response, String token) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(token, Duration.ofMillis(expirationMs)).toString());
    }

    public void clearTokenCookie(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("", Duration.ZERO).toString());
    }

    public String extractToken(HttpServletRequest request) {
        var cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (var cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private ResponseCookie buildCookie(String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
