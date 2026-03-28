package ua.lviv.bas.cinema.config.security.jwt;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.config.security.user.CustomUserDetails;

@Slf4j
@Component
public class JwtTokenProvider {

	@Value("${app.jwt.secret}")
	private String jwtSecret;

	@Value("${app.jwt.expiration}")
	private long jwtExpiration;

	private SecretKey getSigningKey() {
		byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	public String generateToken(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		Map<String, Object> claims = new HashMap<>();
		String subject;

		if (principal instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails) principal;
			claims.put("userId", userDetails.getUserId());
			claims.put("role", userDetails.getRole());
			claims.put("enabled", userDetails.isEnabled());
			subject = userDetails.getUsername();
		} else if (principal instanceof OAuth2User) {
			OAuth2User oAuth2User = (OAuth2User) principal;
			String email = (String) oAuth2User.getAttributes().get("email");
			subject = email;
		} else {
			subject = principal.toString();
		}

		Instant now = Instant.now();
		Instant expiry = now.plus(jwtExpiration, ChronoUnit.MILLIS);

		return Jwts.builder().claims(claims).subject(subject).issuedAt(Date.from(now)).expiration(Date.from(expiry))
				.signWith(getSigningKey()).compact();
	}

	public String getEmailFromToken(String token) {
		Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
		return claims.getSubject();
	}

	public Long getUserIdFromToken(String token) {
		try {
			Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();

			return claims.get("userId", Long.class);
		} catch (Exception e) {
			return null;
		}
	}

	public String getRoleFromToken(String token) {
		try {
			Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
			return claims.get("role", String.class);
		} catch (Exception e) {
			log.error("Error extracting role from token: {}", e.getMessage());
			return null;
		}
	}

	public boolean isEnabledFromToken(String token) {
		try {
			Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
			Boolean enabled = claims.get("enabled", Boolean.class);
			return enabled != null ? enabled : false;
		} catch (Exception e) {
			log.error("Error extracting enabled from token: {}", e.getMessage());
			return false;
		}
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
			return true;
		} catch (Exception ex) {
			log.error("JWT token validation failed: {}", ex.getMessage());
			return false;
		}
	}
}