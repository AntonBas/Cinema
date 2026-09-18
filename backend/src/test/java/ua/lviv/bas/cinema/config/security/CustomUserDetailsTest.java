package ua.lviv.bas.cinema.config.security;

import org.junit.jupiter.api.Test;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    @Test
    void isAccountNonLockedShouldAlwaysBeTrueRegardlessOfEnabledFlag() {
        var enabledUser = new CustomUserDetails(1L, "user@test.com", "hash", true, "ROLE_USER", 0);
        var disabledUser = new CustomUserDetails(2L, "unverified@test.com", "hash", false, "ROLE_USER", 0);

        assertThat(enabledUser.isAccountNonLocked()).isTrue();
        assertThat(disabledUser.isAccountNonLocked())
                .as("account locking is not a feature of this app; an unverified user must fail on isEnabled(), not isAccountNonLocked()")
                .isTrue();
    }

    @Test
    void isEnabledShouldReflectTheEnabledFlag() {
        var enabledUser = new CustomUserDetails(1L, "user@test.com", "hash", true, "ROLE_USER", 0);
        var disabledUser = new CustomUserDetails(2L, "unverified@test.com", "hash", false, "ROLE_USER", 0);

        assertThat(enabledUser.isEnabled()).isTrue();
        assertThat(disabledUser.isEnabled()).isFalse();
    }

    @Test
    void constructorFromUserShouldCarryTokenVersion() {
        User user = User.builder().id(1L).email("user@test.com").password("hash").enabled(true)
                .userRole(UserRole.ROLE_USER).tokenVersion(3).build();

        var userDetails = new CustomUserDetails(user);

        assertThat(userDetails.getTokenVersion()).isEqualTo(3);
    }
}
