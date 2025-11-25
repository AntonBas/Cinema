package ua.lviv.bas.cinema.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private CustomUserDetailsService userDetailsService;

	private User user;
	private String email;

	@BeforeEach
	void setUp() {
		email = "anton@example.com";
		user = User.builder().email(email).password("encodedPassword").userRole(UserRole.ROLE_USER).enabled(true)
				.build();
	}

	@Test
	void loadUserByUsername_ShouldReturnDetails_WhenUserExists() {
		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

		UserDetails userDetails = userDetailsService.loadUserByUsername(email);

		assertNotNull(userDetails);
		assertEquals(email, userDetails.getUsername());
		assertEquals("encodedPassword", userDetails.getPassword());
		assertTrue(userDetails.isEnabled());
		assertTrue(userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));

		verify(userRepository).findByEmail(email);
	}

	@Test
	void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
		String nonExistentEmail = "nonexistent@example.com";
		when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

		assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(nonExistentEmail));

		verify(userRepository).findByEmail(nonExistentEmail);
	}

	@Test
	void loadUserByUsername_ShouldHandleAdminRole() {
		String adminEmail = "admin@example.com";
		User adminUser = User.builder().email(adminEmail).password("encodedPassword").userRole(UserRole.ROLE_ADMIN)
				.enabled(true).build();

		when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(adminUser));

		UserDetails userDetails = userDetailsService.loadUserByUsername(adminEmail);

		assertNotNull(userDetails);
		assertTrue(userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));

		verify(userRepository).findByEmail(adminEmail);
	}

	@Test
	void loadUserByUsername_ShouldHandleDisabledAccount() {
		String disabledEmail = "disabled@example.com";
		User disabledUser = User.builder().email(disabledEmail).password("encodedPassword").userRole(UserRole.ROLE_USER)
				.enabled(false).build();

		when(userRepository.findByEmail(disabledEmail)).thenReturn(Optional.of(disabledUser));

		UserDetails userDetails = userDetailsService.loadUserByUsername(disabledEmail);

		assertNotNull(userDetails);
		assertFalse(userDetails.isEnabled());

		verify(userRepository).findByEmail(disabledEmail);
	}

	@Test
	void loadUserById_ShouldReturnDetails_WhenUserExists() {
		Long userId = 1L;
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		UserDetails userDetails = userDetailsService.loadUserById(userId);

		assertNotNull(userDetails);
		assertEquals(email, userDetails.getUsername());
		verify(userRepository).findById(userId);
	}

	@Test
	void loadUserById_ShouldThrowException_WhenUserNotFound() {
		Long userId = 999L;
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserById(userId));

		verify(userRepository).findById(userId);
	}
}