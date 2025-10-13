package ua.lviv.bas.cinema.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

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

	@Test
	void loadUserByUserName_ShouldReturnDetails_WhenUserExists() {
		String email = "anton@example.com";
		User user = User.builder().email(email).password("encodedPassword").userRole(UserRole.ROLE_USER).enabled(true)
				.build();

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
		String email = "noneexistent@example.com";
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(email));

		verify(userRepository).findByEmail(email);
	}

	@Test
	void loadUserByUsername_ShouldHandleAdminRole() {
		String email = "admin@example.com";
		User user = User.builder().email(email).password("encodedPassword").userRole(UserRole.ROLE_ADMIN).enabled(true)
				.build();

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

		UserDetails userDetails = userDetailsService.loadUserByUsername(email);

		assertNotNull(userDetails);
		assertTrue(userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));

		verify(userRepository).findByEmail(email);
	}

	@Test
	void loadUserByUsername_ShouldHandleDisabledAccount() {
		String email = "disabled@example.com";
		User user = User.builder().email(email).password("encodedPassword").userRole(UserRole.ROLE_USER).enabled(false)
				.build();

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

		UserDetails userDetails = userDetailsService.loadUserByUsername(email);

		assertNotNull(userDetails);
		assertFalse(userDetails.isEnabled());

		verify(userRepository).findByEmail(email);
	}
}
