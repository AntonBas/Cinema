package ua.lviv.bas.cinema.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;

public class CustomUserDetailsTest {

	private User user;
	private CustomUserDetails userDetails;

	@BeforeEach
	void setUp() {
		user = User.builder().id(1L).email("anton@example.com").password("encodedPassword123").firstName("Anton")
				.lastName("Shevchenko").userRole(UserRole.ROLE_USER).enabled(true).build();
		userDetails = new CustomUserDetails(user);
	}

	@Test
	void getAuthorities_ShouldReturnCorrectRole() {
		assertNotNull(userDetails.getAuthorities());
		assertEquals(1, userDetails.getAuthorities().size());

		GrantedAuthority authority = userDetails.getAuthorities().iterator().next();
		assertEquals("ROLE_USER", authority.getAuthority());
	}

	@Test
	void getAuthorities_ShouldReturnAdminRole() {
		User adminUser = User.builder().email("admin@example.com").password("encodedPassword")
				.userRole(UserRole.ROLE_ADMIN).enabled(true).build();
		CustomUserDetails adminDetails = new CustomUserDetails(adminUser);

		GrantedAuthority authority = adminDetails.getAuthorities().iterator().next();
		assertEquals("ROLE_ADMIN", authority.getAuthority());
	}

	@Test
	void getUsername_ShouldReturnEmail() {
		assertEquals("anton@example.com", userDetails.getUsername());
	}

	@Test
	void getPassword_ShouldReturnEncodedPassword() {
		assertEquals("encodedPassword123", userDetails.getPassword());
	}

	@Test
	void isEnabled_ShouldReturnUserEnabledStatus() {
		assertTrue(userDetails.isEnabled());

		User disabledUser = User.builder().email("disabled@example.com").password("encodedPassword")
				.userRole(UserRole.ROLE_USER).enabled(false).build();
		CustomUserDetails disabledDetails = new CustomUserDetails(disabledUser);
		assertFalse(disabledDetails.isEnabled());
	}

	@Test
	void isAccountNonLocked_ShouldReturnEnabledStatus() {
		assertTrue(userDetails.isAccountNonLocked());

		User disabledUser = User.builder().email("disabled@example.com").password("encodedPassword")
				.userRole(UserRole.ROLE_USER).enabled(false).build();
		CustomUserDetails disabledDetails = new CustomUserDetails(disabledUser);
		assertFalse(disabledDetails.isAccountNonLocked());
	}

	@Test
	void accountStatusMethods_ShouldReturnCorrectValues() {
		assertTrue(userDetails.isAccountNonExpired());
		assertTrue(userDetails.isCredentialsNonExpired());
	}

	@Test
	void getUser_ShouldReturnUser() {
		assertEquals(user, userDetails.getUser());
	}

	@Test
	void getUserId_ShouldReturnUserId() {
		assertEquals(1L, userDetails.getUserId());
	}

	@Test
	void getFullName_ShouldReturnConcatenatedName() {
		assertEquals("Anton Shevchenko", userDetails.getFullName());
	}

	@Test
	void isAdmin_ShouldReturnTrueForAdminUser() {
		User adminUser = User.builder().email("admin@example.com").password("encodedPassword")
				.userRole(UserRole.ROLE_ADMIN).enabled(true).build();
		CustomUserDetails adminDetails = new CustomUserDetails(adminUser);

		assertTrue(adminDetails.isAdmin());
		assertFalse(userDetails.isAdmin());
	}

	@Test
	void isCashier_ShouldReturnTrueForCashierUser() {
		User cashierUser = User.builder().email("cashier@example.com").password("encodedPassword")
				.userRole(UserRole.ROLE_CASHIER).enabled(true).build();
		CustomUserDetails cashierDetails = new CustomUserDetails(cashierUser);

		assertTrue(cashierDetails.isCashier());
		assertFalse(userDetails.isCashier());
	}
}