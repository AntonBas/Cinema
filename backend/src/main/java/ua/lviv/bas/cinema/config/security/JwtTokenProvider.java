package ua.lviv.bas.cinema.config.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.security.CustomUserDetails;

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
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		Instant now = Instant.now();
		Instant expiry = now.plus(jwtExpiration, ChronoUnit.MILLIS);

		Map<String, Object> claims = new HashMap<>();
		claims.put("userId", userDetails.getUserId());
		claims.put("role", userDetails.getRole());
		claims.put("enabled", userDetails.isEnabled());

		return Jwts.builder().claims(claims).subject(userDetails.getUsername()).issuedAt(Date.from(now))
				.expiration(Date.from(expiry)).signWith(getSigningKey()).compact();
	}

	public String getEmailFromToken(String token) {
		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload().getSubject();
	}

	public Long getUserIdFromToken(String token) {
		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload().get("userId",
				Long.class);
	}

	public String getRoleFromToken(String token) {
		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload().get("role",
				String.class);
	}

	public boolean isEnabledFromToken(String token) {
		Boolean enabled = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload()
				.get("enabled", Boolean.class);
		return enabled != null ? enabled : false;
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