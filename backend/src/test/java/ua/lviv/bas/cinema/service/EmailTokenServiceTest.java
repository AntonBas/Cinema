package ua.lviv.bas.cinema.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.domain.EmailToken;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.exception.EmailAlreadyExistsException;
import ua.lviv.bas.cinema.exception.InvalidTokenException;
import ua.lviv.bas.cinema.exception.TokenAlreadyConfirmedException;
import ua.lviv.bas.cinema.exception.TokenExpiredException;
import ua.lviv.bas.cinema.repository.EmailTokenRepository;
import ua.lviv.bas.cinema.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailTokenServiceTest {

	@Mock
	private EmailTokenRepository tokenRepository;

	@Mock
	private EmailService emailService;

	@Mock
	private UserService userService;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private EmailTokenService emailTokenService;

	@Test
	void confirmEmail_ShouldConfirmEmail_WhenTokenIsValid() {
		String token = "valid-token";
		User user = new User();
		user.setId(1L);
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
		verify(tokenRepository).save(emailToken);
	}

	@Test
	void confirmEmail_ShouldThrowInvalidTokenException_WhenTokenIsInvalid() {
		String invalidToken = "invalid-token";

		when(tokenRepository.findByToken(invalidToken)).thenReturn(Optional.empty());

		assertThrows(InvalidTokenException.class, () -> emailTokenService.confirmEmail(invalidToken));
		verify(userService, never()).updateUser(any(User.class));
		verify(tokenRepository, never()).save(any(EmailToken.class));
	}

	@Test
	void confirmEmail_ShouldThrowTokenAlreadyConfirmedException_WhenTokenAlreadyConfirmed() {
		String token = "confirmed-token";
		User user = new User();
		user.setEnabled(true);

		EmailToken emailToken = new EmailToken();
		emailToken.setToken(token);
		emailToken.setConfirmed(true);
		emailToken.setConfirmedAt(LocalDateTime.now().minusHours(1));
		emailToken.setExpiresAt(LocalDateTime.now().plusHours(1));
		emailToken.setUser(user);

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));

		assertThrows(TokenAlreadyConfirmedException.class, () -> emailTokenService.confirmEmail(token));
		verify(userService, never()).updateUser(any(User.class));
		verify(tokenRepository, never()).save(any(EmailToken.class));
	}

	@Test
	void confirmEmail_ShouldThrowTokenExpiredException_WhenTokenExpired() {
		String token = "expired-token";
		User user = new User();
		user.setEnabled(false);

		EmailToken emailToken = new EmailToken();
		emailToken.setToken(token);
		emailToken.setConfirmed(false);
		emailToken.setExpiresAt(LocalDateTime.now().minusHours(1));
		emailToken.setUser(user);

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));

		assertThrows(TokenExpiredException.class, () -> emailTokenService.confirmEmail(token));
		verify(userService, never()).updateUser(any(User.class));
		verify(tokenRepository, never()).save(any(EmailToken.class));
	}

	@Test
	void confirmEmailChange_ShouldChangeEmail_WhenTokenIsValid() {
		String token = "valid-email-change-token";
		String oldEmail = "old@example.com";
		String newEmail = "new@example.com";

		User user = new User();
		user.setId(1L);
		user.setEmail(oldEmail);
		user.setEnabled(true);

		EmailToken emailToken = new EmailToken();
		emailToken.setToken(token);
		emailToken.setConfirmed(false);
		emailToken.setExpiresAt(LocalDateTime.now().plusHours(1));
		emailToken.setUser(user);
		emailToken.setNewEmail(newEmail);

		User updatedUser = new User();
		updatedUser.setId(1L);
		updatedUser.setEmail(newEmail);
		updatedUser.setEnabled(true);

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));
		when(userService.existsByEmail(newEmail)).thenReturn(false);
		when(userRepository.save(user)).thenReturn(updatedUser);

		User result = emailTokenService.confirmEmailChange(token);

		assertNotNull(result);
		assertEquals(newEmail, result.getEmail());
		assertTrue(emailToken.isConfirmed());
		assertNotNull(emailToken.getConfirmedAt());

		verify(userService).existsByEmail(newEmail);
		verify(userRepository).save(user);
		verify(emailService).sendEmailChangeNotification(oldEmail, newEmail);
		verify(tokenRepository).save(emailToken);
	}

	@Test
	void confirmEmailChange_ShouldThrowInvalidTokenException_WhenTokenIsInvalid() {
		String invalidToken = "invalid-token";

		when(tokenRepository.findByToken(invalidToken)).thenReturn(Optional.empty());

		assertThrows(InvalidTokenException.class, () -> emailTokenService.confirmEmailChange(invalidToken));
		verify(userService, never()).existsByEmail(anyString());
		verify(userRepository, never()).save(any(User.class));
		verify(emailService, never()).sendEmailChangeNotification(anyString(), anyString());
	}

	@Test
	void confirmEmailChange_ShouldThrowTokenAlreadyConfirmedException_WhenTokenAlreadyConfirmed() {
		String token = "confirmed-email-change-token";
		String oldEmail = "old@example.com";
		String newEmail = "new@example.com";

		User user = new User();
		user.setId(1L);
		user.setEmail(oldEmail);

		EmailToken emailToken = new EmailToken();
		emailToken.setToken(token);
		emailToken.setConfirmed(true);
		emailToken.setConfirmedAt(LocalDateTime.now().minusHours(1));
		emailToken.setExpiresAt(LocalDateTime.now().plusHours(1));
		emailToken.setUser(user);
		emailToken.setNewEmail(newEmail);

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));

		assertThrows(TokenAlreadyConfirmedException.class, () -> emailTokenService.confirmEmailChange(token));
		verify(userService, never()).existsByEmail(anyString());
		verify(userRepository, never()).save(any(User.class));
		verify(emailService, never()).sendEmailChangeNotification(anyString(), anyString());
	}

	@Test
	void confirmEmailChange_ShouldThrowTokenExpiredException_WhenTokenExpired() {
		String token = "expired-email-change-token";
		String oldEmail = "old@example.com";
		String newEmail = "new@example.com";

		User user = new User();
		user.setId(1L);
		user.setEmail(oldEmail);

		EmailToken emailToken = new EmailToken();
		emailToken.setToken(token);
		emailToken.setConfirmed(false);
		emailToken.setExpiresAt(LocalDateTime.now().minusHours(1));
		emailToken.setUser(user);
		emailToken.setNewEmail(newEmail);

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));

		assertThrows(TokenExpiredException.class, () -> emailTokenService.confirmEmailChange(token));
		verify(userService, never()).existsByEmail(anyString());
		verify(userRepository, never()).save(any(User.class));
		verify(emailService, never()).sendEmailChangeNotification(anyString(), anyString());
	}

	@Test
	void confirmEmailChange_ShouldThrowEmailAlreadyExistsException_WhenNewEmailAlreadyExists() {
		String token = "email-change-token";
		String oldEmail = "old@example.com";
		String newEmail = "existing@example.com";

		User user = new User();
		user.setId(1L);
		user.setEmail(oldEmail);

		EmailToken emailToken = new EmailToken();
		emailToken.setToken(token);
		emailToken.setConfirmed(false);
		emailToken.setExpiresAt(LocalDateTime.now().plusHours(1));
		emailToken.setUser(user);
		emailToken.setNewEmail(newEmail);

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));
		when(userService.existsByEmail(newEmail)).thenReturn(true);

		assertThrows(EmailAlreadyExistsException.class, () -> emailTokenService.confirmEmailChange(token));
		verify(userService).existsByEmail(newEmail);
		verify(userRepository, never()).save(any(User.class));
		verify(emailService, never()).sendEmailChangeNotification(anyString(), anyString());
		verify(tokenRepository, never()).save(any(EmailToken.class));
	}

	@Test
	void confirmEmailChange_ShouldThrowInvalidTokenException_WhenNewEmailIsNull() {
		String token = "invalid-email-change-token";
		String oldEmail = "old@example.com";

		User user = new User();
		user.setId(1L);
		user.setEmail(oldEmail);

		EmailToken emailToken = new EmailToken();
		emailToken.setToken(token);
		emailToken.setConfirmed(false);
		emailToken.setExpiresAt(LocalDateTime.now().plusHours(1));
		emailToken.setUser(user);
		emailToken.setNewEmail(null);

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));

		assertThrows(InvalidTokenException.class, () -> emailTokenService.confirmEmailChange(token));
		verify(userService, never()).existsByEmail(anyString());
		verify(userRepository, never()).save(any(User.class));
		verify(emailService, never()).sendEmailChangeNotification(anyString(), anyString());
	}
}