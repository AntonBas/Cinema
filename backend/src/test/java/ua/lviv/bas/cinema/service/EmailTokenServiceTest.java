package ua.lviv.bas.cinema.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.domain.EmailToken;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.repository.EmailTokenRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailTokenServiceTest {

	@Mock
	private EmailTokenRepository tokenRepository;

	@Mock
	private UserService userService;

	@InjectMocks
	private EmailTokenService emailTokenService;

	@Test
	void confirmEmail_ShouldConfirmEmail_WhenTokenIsValid() {
		String token = "valid-token";
		User user = new User();
		user.setEnabled(false);
		user.setEmail("test@example.com");

		EmailToken emailToken = new EmailToken();
		emailToken.setToken(token);
		emailToken.setConfirmed(false);
		emailToken.setExpiresAt(LocalDateTime.now().plusHours(1));
		emailToken.setUser(user);

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));

		String result = emailTokenService.confirmEmail(token);

		assertEquals("Email successfully verified! You can now log in.", result);
		assertTrue(user.isEnabled());
		assertTrue(emailToken.isConfirmed());
		assertNotNull(emailToken.getConfirmedAt());
		verify(userService).updateUser(user);
	}

	@Test
	void confirmEmail_ShouldThrowException_WhenTokenIsInvalid() {
		String invalidToken = "invalid-token";

		when(tokenRepository.findByToken(invalidToken)).thenReturn(Optional.empty());

		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> emailTokenService.confirmEmail(invalidToken));

		assertEquals("Invalid token", exception.getMessage());
		verify(userService, never()).updateUser(any(User.class));
	}

	@Test
	void confirmEmail_ShouldReturnMessage_WhenTokenAlreadyConfirmed() {
		String token = "confirmed-token";
		User user = new User();
		user.setEnabled(true);

		EmailToken emailToken = new EmailToken();
		emailToken.setToken(token);
		emailToken.setConfirmed(true);
		emailToken.setExpiresAt(LocalDateTime.now().plusHours(1));
		emailToken.setUser(user);

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));

		String result = emailTokenService.confirmEmail(token);

		assertEquals("Email already confirmed", result);
		verify(userService, never()).updateUser(any(User.class));
	}

	@Test
	void confirmEmail_ShouldReturnMessage_WhenTokenExpired() {
		String token = "expired-token";
		User user = new User();
		user.setEnabled(false);

		EmailToken emailToken = new EmailToken();
		emailToken.setToken(token);
		emailToken.setConfirmed(false);
		emailToken.setExpiresAt(LocalDateTime.now().minusHours(1));
		emailToken.setUser(user);

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));

		String result = emailTokenService.confirmEmail(token);

		assertEquals("Token expired", result);
		verify(userService, never()).updateUser(any(User.class));
	}

	@Test
	void confirmEmail_ShouldUpdateUserAndToken_WhenSuccessfulConfirmation() {
		String token = "success-token";
		User user = new User();
		user.setEnabled(false);
		user.setEmail("user@example.com");

		EmailToken emailToken = new EmailToken();
		emailToken.setToken(token);
		emailToken.setConfirmed(false);
		emailToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
		emailToken.setUser(user);

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));

		emailTokenService.confirmEmail(token);

		verify(userService).updateUser(user);
		assertTrue(emailToken.isConfirmed());
		assertNotNull(emailToken.getConfirmedAt());
	}
}