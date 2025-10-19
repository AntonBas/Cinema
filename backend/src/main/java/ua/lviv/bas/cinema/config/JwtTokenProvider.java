package ua.lviv.bas.cinema.config;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
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
			byte[] keyBytes = jwtSecret.getBytes();
			if (keyBytes.length < 64) {
				byte[] paddedKey = new byte[64];
				System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 64));
				signingKey = Keys.hmacShaKeyFor(paddedKey);
			} else {
				signingKey = Keys.hmacShaKeyFor(keyBytes);
			}
		}
		return signingKey;
	}

	public String generateToken(Authentication authentication) {
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + jwtExpiration);

		return Jwts.builder().setSubject(userDetails.getUsername()).setIssuedAt(now).setExpiration(expiryDate)
				.signWith(getSigningKey()).compact();
	}

	public String getEmailFromToken(String token) {
		Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
		return claims.getSubject();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
			return true;
		} catch (ExpiredJwtException ex) {
			log.error("JWT token is expired: {}", ex.getMessage());
		} catch (Exception ex) {
			log.error("JWT token validation failed: {}", ex.getMessage());
		}
		return false;
	}

	public Date getExpirationFromToken(String token) {
		Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
		return claims.getExpiration();
	}
}