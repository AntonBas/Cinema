package ua.lviv.bas.cinema.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret",
                Base64.getEncoder().encodeToString("test-secret-key-that-is-at-least-32-bytes-long!".getBytes()));
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 3_600_000L);
    }

    @Test
    void generateTokenShouldEmbedTokenVersionClaimForCustomUserDetails() {
        var userDetails = new CustomUserDetails(1L, "user@test.com", "hash", true, "ROLE_USER", 5);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());

        String token = jwtTokenProvider.generateToken(authentication);

        assertThat(jwtTokenProvider.getEmailFromToken(token)).isEqualTo("user@test.com");
        assertThat(jwtTokenProvider.getTokenVersionFromToken(token)).isEqualTo(5);
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void getTokenVersionFromTokenShouldReturnNullWhenClaimAbsent() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("plain-subject@test.com", null,
                java.util.List.of());

        String token = jwtTokenProvider.generateToken(authentication);

        assertThat(jwtTokenProvider.getTokenVersionFromToken(token)).isNull();
    }

    @Test
    void validateTokenShouldReturnFalseForGarbageInput() {
        assertThat(jwtTokenProvider.validateToken("not-a-jwt")).isFalse();
    }

    @Test
    void generateTokenShouldEmbedUniqueJti() {
        var userDetails = new CustomUserDetails(1L, "user@test.com", "hash", true, "ROLE_USER", 0);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());

        String tokenOne = jwtTokenProvider.generateToken(authentication);
        String tokenTwo = jwtTokenProvider.generateToken(authentication);

        assertThat(jwtTokenProvider.getJtiFromToken(tokenOne)).isNotBlank();
        assertThat(jwtTokenProvider.getJtiFromToken(tokenOne)).isNotEqualTo(jwtTokenProvider.getJtiFromToken(tokenTwo));
    }

    @Test
    void getExpirationFromTokenShouldMatchConfiguredExpiration() {
        var userDetails = new CustomUserDetails(1L, "user@test.com", "hash", true, "ROLE_USER", 0);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        Instant before = Instant.now();

        String token = jwtTokenProvider.generateToken(authentication);

        Instant expiration = jwtTokenProvider.getExpirationFromToken(token);
        assertThat(expiration).isAfter(before.plusSeconds(3_500)).isBefore(before.plusSeconds(3_700));
    }
}
