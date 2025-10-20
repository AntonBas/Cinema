package ua.lviv.bas.cinema.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import ua.lviv.bas.cinema.domain.EmailToken;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.repository.EmailTokenRepository;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

	@Mock
	private EmailTokenGeneratorService tokenGeneratorService;

	@Mock
	private UserService userService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private EmailTokenRepository tokenRepository;

	@InjectMocks
	private PasswordResetService passwordResetService;

	@Test
	void requestPasswordReset_ShouldGenerateToken_WhenUserExists() {
		String email = "test@example.com";

		passwordResetService.requestPasswordReset(email);

		verify(userService).findByEmail(email);
		verify(tokenGeneratorService).generatePasswordResetToken(email);
	}

	@Test
	void resetPassword_ShouldResetPassword_WhenTokenIsValid() {
		String token = "valid-token";
		String newPassword = "newPassword123";
		User user = new User();
		user.setPassword("oldPassword");

		EmailToken resetToken = new EmailToken();
		resetToken.setToken(token);
		resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
		resetToken.setUser(user);

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
		when(passwordEncoder.encode(newPassword)).thenReturn("encodedPassword");

		passwordResetService.resetPassword(token, newPassword);

		assertEquals("encodedPassword", user.getPassword());
		verify(tokenRepository).delete(resetToken);
		verify(passwordEncoder).encode(newPassword);
	}

	@Test
	void resetPassword_ShouldThrowException_WhenTokenIsInvalid() {
		String token = "invalid-token";
		String newPassword = "newPassword123";

		when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> passwordResetService.resetPassword(token, newPassword));

		assertEquals("Invalid token", exception.getMessage());
		verify(tokenRepository, never()).delete(any());
		verify(passwordEncoder, never()).encode(any());
	}

	@Test
	void resetPassword_ShouldThrowException_WhenTokenExpired() {
		String token = "expired-token";
		String newPassword = "newPassword123";
		User user = new User();

		EmailToken resetToken = new EmailToken();
		resetToken.setToken(token);
		resetToken.setExpiresAt(LocalDateTime.now().minusHours(1));
		resetToken.setUser(user);

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> passwordResetService.resetPassword(token, newPassword));

		assertEquals("Token expired", exception.getMessage());
		verify(tokenRepository, never()).delete(any());
		verify(passwordEncoder, never()).encode(any());
	}

	@Test
	void resetPassword_ShouldThrowException_WhenPasswordTooShort() {
		String token = "valid-token";
		String shortPassword = "123";

		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> passwordResetService.resetPassword(token, shortPassword));

		assertEquals("Password must be at least 6 characters", exception.getMessage());
		verify(tokenRepository, never()).findByToken(any());
		verify(passwordEncoder, never()).encode(any());
	}

	@Test
	void resetPassword_ShouldEncodePassword_WhenSuccessfulReset() {
		String token = "valid-token";
		String newPassword = "validPassword";
		User user = new User();
		user.setPassword("oldEncodedPassword");

		EmailToken resetToken = new EmailToken();
		resetToken.setToken(token);
		resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
		resetToken.setUser(user);

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
		when(passwordEncoder.encode(newPassword)).thenReturn("newEncodedPassword");

		passwordResetService.resetPassword(token, newPassword);

		verify(passwordEncoder).encode(newPassword);
		assertEquals("newEncodedPassword", user.getPassword());
		verify(tokenRepository).delete(resetToken);
	}
}