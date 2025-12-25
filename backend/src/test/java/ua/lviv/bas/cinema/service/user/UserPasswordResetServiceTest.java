package ua.lviv.bas.cinema.service.user;

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
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.exception.domain.auth.InvalidTokenException;
import ua.lviv.bas.cinema.exception.domain.auth.SamePasswordException;
import ua.lviv.bas.cinema.exception.domain.auth.TokenExpiredException;
import ua.lviv.bas.cinema.exception.domain.user.EmailNotVerifiedException;
import ua.lviv.bas.cinema.repository.EmailTokenRepository;
import ua.lviv.bas.cinema.repository.UserRepository;
import ua.lviv.bas.cinema.service.common.EmailTokenGeneratorService;

@ExtendWith(MockitoExtension.class)
class UserPasswordResetServiceTest {

	@Mock
	private EmailTokenGeneratorService tokenGeneratorService;

	@Mock
	private UserService userService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private EmailTokenRepository tokenRepository;

	@InjectMocks
	private UserPasswordResetService passwordResetService;

	@Test
	void requestPasswordReset_ShouldGenerateToken_WhenUserExistsAndVerified() {
		String email = "test@example.com";
		User user = new User();
		user.setEmail(email);
		user.setVerificationStatus(VerificationStatus.VERIFIED);

		when(userService.findByEmail(email)).thenReturn(user);

		passwordResetService.requestPasswordReset(email);

		verify(userService).findByEmail(email);
		verify(tokenGeneratorService).generatePasswordResetToken(email);
	}

	@Test
	void requestPasswordReset_ShouldThrowException_WhenUserNotVerified() {
		String email = "test@example.com";
		User user = new User();
		user.setEmail(email);
		user.setVerificationStatus(VerificationStatus.NOT_VERIFIED);

		when(userService.findByEmail(email)).thenReturn(user);

		assertThrows(EmailNotVerifiedException.class, () -> passwordResetService.requestPasswordReset(email));

		verify(userService).findByEmail(email);
		verify(tokenGeneratorService, never()).generatePasswordResetToken(any());
	}

	@Test
	void resetPassword_ShouldResetPassword_WhenTokenIsValid() {
		String token = "valid-token";
		String newPassword = "newPassword123";

		User user = new User();
		user.setEmail("test@example.com");
		user.setPassword("oldEncodedPassword");
		user.setVerificationStatus(VerificationStatus.VERIFIED);

		EmailToken resetToken = EmailToken.builder().token(token).expiresAt(LocalDateTime.now().plusHours(1)).user(user)
				.build();

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
		when(passwordEncoder.matches(newPassword, user.getPassword())).thenReturn(false);
		when(passwordEncoder.encode(newPassword)).thenReturn("newEncodedPassword");

		passwordResetService.resetPassword(token, newPassword);

		verify(passwordEncoder).encode(newPassword);
		verify(userRepository).save(user);
		verify(tokenRepository).save(resetToken);
		verify(tokenRepository).findByToken(token);
	}

	@Test
	void resetPassword_ShouldThrowInvalidTokenException_WhenTokenIsInvalid() {
		String token = "invalid-token";
		String newPassword = "newPassword123";

		when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

		assertThrows(InvalidTokenException.class, () -> passwordResetService.resetPassword(token, newPassword));

		verify(tokenRepository).findByToken(token);
		verify(passwordEncoder, never()).encode(any());
		verify(userRepository, never()).save(any());
		verify(tokenRepository, never()).save(any());
	}

	@Test
	void resetPassword_ShouldThrowTokenExpiredException_WhenTokenExpired() {
		String token = "expired-token";
		String newPassword = "newPassword123";

		User user = new User();
		user.setEmail("test@example.com");

		EmailToken resetToken = EmailToken.builder().token(token).expiresAt(LocalDateTime.now().minusHours(1))
				.user(user).build();

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

		assertThrows(TokenExpiredException.class, () -> passwordResetService.resetPassword(token, newPassword));

		verify(tokenRepository).findByToken(token);
		verify(passwordEncoder, never()).encode(any());
		verify(userRepository, never()).save(any());
		verify(tokenRepository, never()).save(any());
	}

	@Test
	void resetPassword_ShouldThrowSamePasswordException_WhenNewPasswordMatchesOld() {
		String token = "valid-token";
		String newPassword = "samePassword";

		User user = new User();
		user.setEmail("test@example.com");
		user.setPassword("oldEncodedPassword");

		EmailToken resetToken = EmailToken.builder().token(token).expiresAt(LocalDateTime.now().plusHours(1)).user(user)
				.build();

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
		when(passwordEncoder.matches(newPassword, user.getPassword())).thenReturn(true);

		assertThrows(SamePasswordException.class, () -> passwordResetService.resetPassword(token, newPassword));

		verify(tokenRepository).findByToken(token);
		verify(passwordEncoder).matches(newPassword, user.getPassword());
		verify(passwordEncoder, never()).encode(any());
		verify(userRepository, never()).save(any());
		verify(tokenRepository, never()).save(any());
	}
}