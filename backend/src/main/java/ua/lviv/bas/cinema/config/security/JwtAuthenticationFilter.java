package ua.lviv.bas.cinema.config.security;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {

		String jwt = getJwtFromRequest(request);

		if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
			String email = jwtTokenProvider.getEmailFromToken(jwt);
			Long userId = jwtTokenProvider.getUserIdFromToken(jwt);
			String role = jwtTokenProvider.getRoleFromToken(jwt);
			boolean enabled = jwtTokenProvider.isEnabledFromToken(jwt);

			log.debug("Authenticated user: {} from JWT", email);

			Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

			JwtUserDetails userDetails = new JwtUserDetails(userId, email, role, enabled, authorities);

			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
					null, userDetails.getAuthorities());
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		filterChain.doFilter(request, response);
	}

	private String getJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}