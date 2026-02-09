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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
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

	private SecretKey signingKey;

	private SecretKey getSigningKey() {
		if (signingKey == null) {
			if (jwtSecret.length() < 32) {
				StringBuilder safeSecret = new StringBuilder(jwtSecret);
				while (safeSecret.length() < 32) {
					safeSecret.append("0");
				}
				jwtSecret = safeSecret.toString();
			}

			byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
			signingKey = Keys.hmacShaKeyFor(keyBytes);
		}
		return signingKey;
	}

	public String generateToken(Authentication authentication) {
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		Instant now = Instant.now();
		Instant expiry = now.plus(jwtExpiration, ChronoUnit.MILLIS);

		Map<String, Object> claims = new HashMap<>();
		claims.put("username", userDetails.getUsername());
		claims.put("userId", userDetails.getUserId());
		claims.put("role", userDetails.getAuthorities());

		return Jwts.builder().claims(claims).subject(userDetails.getUsername()).issuedAt(Date.from(now))
				.expiration(Date.from(expiry)).signWith(getSigningKey(), Jwts.SIG.HS256).compact();
	}

	public String getEmailFromToken(String token) {
		Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();

		return claims.getSubject();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
			return true;
		} catch (ExpiredJwtException ex) {
			log.error("JWT token is expired: {}", ex.getMessage());
		} catch (Exception ex) {
			log.error("JWT token validation failed: {}", ex.getMessage());
		}
		return false;
	}

	public Date getExpirationFromToken(String token) {
		Claims claims = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();

		return claims.getExpiration();
	}
}