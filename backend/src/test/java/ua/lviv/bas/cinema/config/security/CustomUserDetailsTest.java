package ua.lviv.bas.cinema.config.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    @Test
    void isAccountNonLockedShouldAlwaysBeTrueRegardlessOfEnabledFlag() {
        var enabledUser = new CustomUserDetails(1L, "user@test.com", "hash", true, "ROLE_USER");
        var disabledUser = new CustomUserDetails(2L, "unverified@test.com", "hash", false, "ROLE_USER");

        assertThat(enabledUser.isAccountNonLocked()).isTrue();
        assertThat(disabledUser.isAccountNonLocked())
                .as("account locking is not a feature of this app; an unverified user must fail on isEnabled(), not isAccountNonLocked()")
                .isTrue();
    }

    @Test
    void isEnabledShouldReflectTheEnabledFlag() {
        var enabledUser = new CustomUserDetails(1L, "user@test.com", "hash", true, "ROLE_USER");
        var disabledUser = new CustomUserDetails(2L, "unverified@test.com", "hash", false, "ROLE_USER");

        assertThat(enabledUser.isEnabled()).isTrue();
        assertThat(disabledUser.isEnabled()).isFalse();
    }
}
