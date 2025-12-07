package ua.lviv.bas.cinema.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

import ua.lviv.bas.cinema.domain.EmailToken;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.exception.domain.auth.EmailAlreadyExistsException;
import ua.lviv.bas.cinema.exception.domain.auth.InvalidTokenException;
import ua.lviv.bas.cinema.exception.domain.auth.TokenAlreadyConfirmedException;
import ua.lviv.bas.cinema.exception.domain.auth.TokenExpiredException;
import ua.lviv.bas.cinema.repository.EmailTokenRepository;
import ua.lviv.bas.cinema.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class EmailTokenServiceTest {

	@Mock
	private EmailTokenRepository tokenRepository;

	@Mock
	private EmailService emailService;

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

		EmailToken emailToken = EmailToken.builder().token(token).confirmed(false)
				.expiresAt(LocalDateTime.now().plusHours(1)).user(user).build();

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));
		when(userRepository.save(user)).thenReturn(user);
		when(tokenRepository.save(emailToken)).thenReturn(emailToken);

		String result = emailTokenService.confirmEmail(token);

		assertEquals("Email successfully verified! You can now log in.", result);
		assertTrue(user.isEnabled());
		assertTrue(emailToken.isConfirmed());
		assertNotNull(emailToken.getConfirmedAt());

		verify(userRepository).save(user);
		verify(tokenRepository).save(emailToken);
	}

	@Test
	void confirmEmail_ShouldThrowInvalidTokenException_WhenTokenIsInvalid() {
		String invalidToken = "invalid-token";
		when(tokenRepository.findByToken(invalidToken)).thenReturn(Optional.empty());

		InvalidTokenException exception = assertThrows(InvalidTokenException.class,
				() -> emailTokenService.confirmEmail(invalidToken));

		assertNotNull(exception);
		verify(userRepository, never()).save(any(User.class));
		verify(tokenRepository, never()).save(any(EmailToken.class));
	}

	@Test
	void confirmEmail_ShouldThrowTokenAlreadyConfirmedException_WhenTokenAlreadyConfirmed() {
		String token = "confirmed-token";
		User user = new User();
		user.setId(1L);
		user.setEnabled(true);
		user.setEmail("test@example.com");

		EmailToken emailToken = EmailToken.builder().token(token).confirmed(true)
				.confirmedAt(LocalDateTime.now().minusHours(1)).expiresAt(LocalDateTime.now().plusHours(1)).user(user)
				.build();

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));

		TokenAlreadyConfirmedException exception = assertThrows(TokenAlreadyConfirmedException.class,
				() -> emailTokenService.confirmEmail(token));

		assertNotNull(exception);
		verify(userRepository, never()).save(any(User.class));
		verify(tokenRepository, never()).save(any(EmailToken.class));
	}

	@Test
	void confirmEmail_ShouldThrowTokenExpiredException_WhenTokenExpired() {
		String token = "expired-token";
		User user = new User();
		user.setId(1L);
		user.setEnabled(false);
		user.setEmail("test@example.com");

		EmailToken emailToken = EmailToken.builder().token(token).confirmed(false)
				.expiresAt(LocalDateTime.now().minusHours(1)).user(user).build();

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));

		TokenExpiredException exception = assertThrows(TokenExpiredException.class,
				() -> emailTokenService.confirmEmail(token));

		assertNotNull(exception);
		verify(userRepository, never()).save(any(User.class));
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

		EmailToken emailToken = EmailToken.builder().token(token).confirmed(false)
				.expiresAt(LocalDateTime.now().plusHours(1)).user(user).newEmail(newEmail).build();

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));
		when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

		User result = emailTokenService.confirmEmailChange(token);

		assertNotNull(result);
		assertEquals(newEmail, result.getEmail());
		assertTrue(emailToken.isConfirmed());
		assertNotNull(emailToken.getConfirmedAt());

		verify(userRepository).findByEmail(newEmail);
		verify(userRepository).save(user);
		verify(emailService).sendEmailChangeNotification(oldEmail, newEmail);
		verify(tokenRepository).save(emailToken);
	}

	@Test
	void confirmEmailChange_ShouldThrowInvalidTokenException_WhenTokenIsInvalid() {
		String invalidToken = "invalid-token";
		when(tokenRepository.findByToken(invalidToken)).thenReturn(Optional.empty());

		InvalidTokenException exception = assertThrows(InvalidTokenException.class,
				() -> emailTokenService.confirmEmailChange(invalidToken));

		assertNotNull(exception);
		verify(userRepository, never()).findByEmail(anyString());
		verify(userRepository, never()).save(any(User.class));
		verify(emailService, never()).sendEmailChangeNotification(anyString(), anyString());
		verify(tokenRepository, never()).save(any(EmailToken.class));
	}

	@Test
	void confirmEmailChange_ShouldThrowTokenAlreadyConfirmedException_WhenTokenAlreadyConfirmed() {
		String token = "confirmed-email-change-token";
		User user = new User();
		user.setId(1L);
		user.setEmail("old@example.com");

		EmailToken emailToken = EmailToken.builder().token(token).confirmed(true)
				.confirmedAt(LocalDateTime.now().minusHours(1)).expiresAt(LocalDateTime.now().plusHours(1)).user(user)
				.newEmail("new@example.com").build();

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));

		TokenAlreadyConfirmedException exception = assertThrows(TokenAlreadyConfirmedException.class,
				() -> emailTokenService.confirmEmailChange(token));

		assertNotNull(exception);
		verify(userRepository, never()).findByEmail(anyString());
		verify(userRepository, never()).save(any(User.class));
		verify(emailService, never()).sendEmailChangeNotification(anyString(), anyString());
		verify(tokenRepository, never()).save(any(EmailToken.class));
	}

	@Test
	void confirmEmailChange_ShouldThrowTokenExpiredException_WhenTokenExpired() {
		String token = "expired-email-change-token";
		User user = new User();
		user.setId(1L);
		user.setEmail("old@example.com");

		EmailToken emailToken = EmailToken.builder().token(token).confirmed(false)
				.expiresAt(LocalDateTime.now().minusHours(1)).user(user).newEmail("new@example.com").build();

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));

		TokenExpiredException exception = assertThrows(TokenExpiredException.class,
				() -> emailTokenService.confirmEmailChange(token));

		assertNotNull(exception);
		verify(userRepository, never()).findByEmail(anyString());
		verify(userRepository, never()).save(any(User.class));
		verify(emailService, never()).sendEmailChangeNotification(anyString(), anyString());
		verify(tokenRepository, never()).save(any(EmailToken.class));
	}

	@Test
	void confirmEmailChange_ShouldThrowEmailAlreadyExistsException_WhenNewEmailAlreadyExists() {
		String token = "email-change-token";
		String oldEmail = "old@example.com";
		String newEmail = "existing@example.com";

		User user = new User();
		user.setId(1L);
		user.setEmail(oldEmail);

		EmailToken emailToken = EmailToken.builder().token(token).confirmed(false)
				.expiresAt(LocalDateTime.now().plusHours(1)).user(user).newEmail(newEmail).build();

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));
		when(userRepository.findByEmail(newEmail)).thenReturn(Optional.of(new User()));

		EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
				() -> emailTokenService.confirmEmailChange(token));

		assertNotNull(exception);
		verify(userRepository).findByEmail(newEmail);
		verify(userRepository, never()).save(any(User.class));
		verify(emailService, never()).sendEmailChangeNotification(anyString(), anyString());
		verify(tokenRepository, never()).save(any(EmailToken.class));
	}

	@Test
	void confirmEmailChange_ShouldThrowInvalidTokenException_WhenNewEmailIsNull() {
		String token = "invalid-email-change-token";
		User user = new User();
		user.setId(1L);
		user.setEmail("old@example.com");

		EmailToken emailToken = EmailToken.builder().token(token).confirmed(false)
				.expiresAt(LocalDateTime.now().plusHours(1)).user(user).build();

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));

		InvalidTokenException exception = assertThrows(InvalidTokenException.class,
				() -> emailTokenService.confirmEmailChange(token));

		assertNotNull(exception);
		verify(userRepository, never()).findByEmail(anyString());
		verify(userRepository, never()).save(any(User.class));
		verify(emailService, never()).sendEmailChangeNotification(anyString(), anyString());
		verify(tokenRepository, never()).save(any(EmailToken.class));
	}

	@Test
	void confirmEmailChange_ShouldThrowIllegalArgumentException_WhenEmailsAreSame() {
		String token = "same-email-token";
		String email = "same@example.com";

		User user = new User();
		user.setId(1L);
		user.setEmail(email);

		EmailToken emailToken = EmailToken.builder().token(token).confirmed(false)
				.expiresAt(LocalDateTime.now().plusHours(1)).user(user).newEmail(email).build();

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> emailTokenService.confirmEmailChange(token));

		assertEquals("New email cannot be the same as current email", exception.getMessage());
		verify(userRepository, never()).findByEmail(anyString());
		verify(userRepository, never()).save(any(User.class));
		verify(emailService, never()).sendEmailChangeNotification(anyString(), anyString());
		verify(tokenRepository, never()).save(any(EmailToken.class));
	}

	@Test
	void confirmEmailChange_ShouldHandleCaseInsensitiveEmailComparison() {
		String token = "case-insensitive-token";
		String oldEmail = "OLD@example.com";
		String newEmail = "old@example.com";

		User user = new User();
		user.setId(1L);
		user.setEmail(oldEmail);

		EmailToken emailToken = EmailToken.builder().token(token).confirmed(false)
				.expiresAt(LocalDateTime.now().plusHours(1)).user(user).newEmail(newEmail).build();

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> emailTokenService.confirmEmailChange(token));

		assertEquals("New email cannot be the same as current email", exception.getMessage());
		verify(userRepository, never()).findByEmail(anyString());
		verify(userRepository, never()).save(any(User.class));
		verify(emailService, never()).sendEmailChangeNotification(anyString(), anyString());
		verify(tokenRepository, never()).save(any(EmailToken.class));
	}
}