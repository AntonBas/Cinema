package ua.lviv.bas.cinema.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        var userDetails = new CustomUserDetails(1L, "user@test.com", "hash", true, true, "ROLE_USER", 5);

        String token = jwtTokenProvider.generateToken(userDetails);

        assertThat(jwtTokenProvider.getEmailFromToken(token)).isEqualTo("user@test.com");
        assertThat(jwtTokenProvider.getTokenVersionFromToken(token)).isEqualTo(5);
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void generateTokenShouldEmbedUserIdClaim() {
        var userDetails = new CustomUserDetails(42L, "user@test.com", "hash", true, true, "ROLE_USER", 0);

        String token = jwtTokenProvider.generateToken(userDetails);

        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(42L);
    }

    @Test
    void validateTokenShouldReturnFalseForGarbageInput() {
        assertThat(jwtTokenProvider.validateToken("not-a-jwt")).isFalse();
    }

    @Test
    void generateTokenShouldEmbedUniqueJti() {
        var userDetails = new CustomUserDetails(1L, "user@test.com", "hash", true, true, "ROLE_USER", 0);

        String tokenOne = jwtTokenProvider.generateToken(userDetails);
        String tokenTwo = jwtTokenProvider.generateToken(userDetails);

        assertThat(jwtTokenProvider.getJtiFromToken(tokenOne)).isNotBlank();
        assertThat(jwtTokenProvider.getJtiFromToken(tokenOne)).isNotEqualTo(jwtTokenProvider.getJtiFromToken(tokenTwo));
    }

    @Test
    void getExpirationFromTokenShouldMatchConfiguredExpiration() {
        var userDetails = new CustomUserDetails(1L, "user@test.com", "hash", true, true, "ROLE_USER", 0);
        Instant before = Instant.now();

        String token = jwtTokenProvider.generateToken(userDetails);

        Instant expiration = jwtTokenProvider.getExpirationFromToken(token);
        assertThat(expiration).isAfter(before.plusSeconds(3_500)).isBefore(before.plusSeconds(3_700));
    }
}
