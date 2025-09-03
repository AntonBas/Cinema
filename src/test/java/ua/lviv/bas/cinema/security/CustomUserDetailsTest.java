package ua.lviv.bas.cinema.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;

public class CustomUserDetailsTest {

	@Test
	void getAuthorities_ShouldReturnCorrectRoles() {
		User user = User.builder().email("anton@example.com").password("encodedPassword").userRole(UserRole.ROLE_USER)
				.enabled(true).build();

		List<String> roles = Collections.singletonList("ROLE_USER");
		CustomUserDetails userDetails = new CustomUserDetails(user, roles);

		assertNotNull(userDetails.getAuthorities());
		assertEquals(1, userDetails.getAuthorities().size());

		GrantedAuthority authority = userDetails.getAuthorities().iterator().next();
		assertEquals("ROLE_USER", authority.getAuthority());
	}

	@Test
	void getAuthorities_ShouldHandleMultipleRoles() {
		User user = User.builder().email("admin@example.com").password("encodedPassword").userRole(UserRole.ROLE_ADMIN)
				.enabled(true).build();

		List<String> roles = List.of("ROLE_ADMIN", "ROLE_MODERATOR");
		CustomUserDetails userDetails = new CustomUserDetails(user, roles);

		assertNotNull(userDetails.getAuthorities());
		assertEquals(2, userDetails.getAuthorities().size());

		assertTrue(userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
		assertTrue(
				userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_MODERATOR")));
	}

	@Test
	void getUsername_ShouldReturnEmail() {
		User user = User.builder().email("anton@example.com").password("encodedPassword").userRole(UserRole.ROLE_USER)
				.enabled(true).build();

		CustomUserDetails userDetails = new CustomUserDetails(user, Collections.singletonList("ROLE_USER"));

		assertEquals("anton@example.com", userDetails.getUsername());
	}

	@Test
	void getPassword_ShouldReturnEncodedPassword() {
		User user = User.builder().email("anton@example.com").password("encodedPassword123")
				.userRole(UserRole.ROLE_USER).enabled(true).build();

		CustomUserDetails userDetails = new CustomUserDetails(user, Collections.singletonList("ROLE_USER"));

		assertEquals("encodedPassword123", userDetails.getPassword());
	}

	@Test
	void isEnabled_ShouldReturnUserEnabledStatus() {
		User enabledUser = User.builder().email("enabled@example.com").password("encodedPassword")
				.userRole(UserRole.ROLE_USER).enabled(true).build();

		User disabledUser = User.builder().email("disabled@example.com").password("encodedPassword")
				.userRole(UserRole.ROLE_USER).enabled(false).build();

		CustomUserDetails enabledDetails = new CustomUserDetails(enabledUser, Collections.singletonList("ROLE_USER"));
		CustomUserDetails disabledDetails = new CustomUserDetails(disabledUser, Collections.singletonList("ROLE_USER"));

		assertTrue(enabledDetails.isEnabled());
		assertFalse(disabledDetails.isEnabled());
	}

	@Test
	void accountStatusMethods_ShouldAlwaysReturnTrue() {
		User user = User.builder().email("anton@example.com").password("encodedPassword").userRole(UserRole.ROLE_USER)
				.enabled(true).build();

		CustomUserDetails userDetails = new CustomUserDetails(user, Collections.singletonList("ROLE_USER"));

		assertTrue(userDetails.isAccountNonExpired());
		assertTrue(userDetails.isAccountNonLocked());
		assertTrue(userDetails.isCredentialsNonExpired());
	}
}
