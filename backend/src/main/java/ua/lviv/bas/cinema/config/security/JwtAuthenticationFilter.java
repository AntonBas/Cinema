package ua.lviv.bas.cinema.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final JwtCookieService jwtCookieService;
    private final JwtBlacklistService jwtBlacklistService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String jwt = jwtCookieService.extractToken(request);
        if (StringUtils.hasText(jwt)) {
            authenticate(jwt, request);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(String jwt, HttpServletRequest request) {
        if (!jwtTokenProvider.validateToken(jwt)) {
            log.warn("Rejected request to {} with invalid or expired JWT", request.getRequestURI());
            return;
        }

        String email = jwtTokenProvider.getEmailFromToken(jwt);
        log.debug("JWT token valid for email: {}", email);

        if (jwtBlacklistService.isBlacklisted(jwtTokenProvider.getJtiFromToken(jwt))) {
            log.warn("Rejected request to {} for blacklisted token (logged out) for user: {}",
                    request.getRequestURI(), email);
            return;
        }

        try {
            UserDetails userDetails = userDetailsService.loadTokenPrincipal(email);
            if (isRejected(userDetails, jwt, email, request)) {
                return;
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Authenticated user: {}", email);
        } catch (Exception e) {
            log.error("Could not load user by email: {}", email, e);
        }
    }

    private boolean isRejected(UserDetails userDetails, String jwt, String email, HttpServletRequest request) {
        if (!userDetails.isEnabled()) {
            log.warn("Rejected request to {} for disabled user: {}", request.getRequestURI(), email);
            return true;
        }
        if (!(userDetails instanceof CustomUserDetails customUserDetails)) {
            return false;
        }
        if (!customUserDetails.getUserId().equals(jwtTokenProvider.getUserIdFromToken(jwt))) {
            log.warn("Rejected request to {} for token issued to another account with email: {}",
                    request.getRequestURI(), email);
            return true;
        }
        Integer tokenVersion = jwtTokenProvider.getTokenVersionFromToken(jwt);
        if (tokenVersion != null && tokenVersion != customUserDetails.getTokenVersion()) {
            log.warn("Rejected request to {} for stale token (password changed since issue) for user: {}",
                    request.getRequestURI(), email);
            return true;
        }
        return false;
    }
}
