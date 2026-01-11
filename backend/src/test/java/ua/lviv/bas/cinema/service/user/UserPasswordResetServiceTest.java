package ua.lviv.bas.cinema.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import ua.lviv.bas.cinema.service.notification.EmailTokenGeneratorService;

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

	@Captor
	private ArgumentCaptor<User> userCaptor;

	@Captor
	private ArgumentCaptor<EmailToken> tokenCaptor;

	@Test
	void requestPasswordReset_ShouldGenerateToken_WhenUserExistsAndEnabled() {
		String email = "test@example.com";
		User user = new User();
		user.setEmail(email);
		user.setEnabled(true);
		user.setVerificationStatus(VerificationStatus.VERIFIED);

		when(userService.getByEmail(email)).thenReturn(user);

		passwordResetService.requestPasswordReset(email);

		verify(userService).getByEmail(email);
		verify(tokenGeneratorService).generatePasswordResetToken(email);
	}

	@Test
	void requestPasswordReset_ShouldThrowException_WhenUserNotEnabled() {
		String email = "test@example.com";
		User user = new User();
		user.setEmail(email);
		user.setEnabled(false);
		user.setVerificationStatus(VerificationStatus.VERIFIED);

		when(userService.getByEmail(email)).thenReturn(user);

		assertThatThrownBy(() -> passwordResetService.requestPasswordReset(email))
				.isInstanceOf(EmailNotVerifiedException.class).hasMessageContaining("Cannot reset password: email");

		verify(userService).getByEmail(email);
		verify(tokenGeneratorService, never()).generatePasswordResetToken(any());
	}

	@Test
	void requestPasswordReset_ShouldThrowException_WhenUserNotVerified() {
		String email = "test@example.com";
		User user = new User();
		user.setEmail(email);
		user.setEnabled(false);
		user.setVerificationStatus(VerificationStatus.NOT_VERIFIED);

		when(userService.getByEmail(email)).thenReturn(user);

		assertThatThrownBy(() -> passwordResetService.requestPasswordReset(email))
				.isInstanceOf(EmailNotVerifiedException.class);

		verify(userService).getByEmail(email);
		verify(tokenGeneratorService, never()).generatePasswordResetToken(any());
	}

	@Test
	void resetPassword_ShouldResetPassword_WhenTokenIsValid() {
		String token = "valid-token";
		String newPassword = "newPassword123";
		String encodedPassword = "encodedNewPassword";

		User user = new User();
		user.setId(1L);
		user.setEmail("test@example.com");
		user.setPassword("oldEncodedPassword");
		user.setEnabled(true);

		EmailToken resetToken = EmailToken.builder().token(token).expiresAt(LocalDateTime.now().plusHours(1)).user(user)
				.confirmed(false).build();

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
		when(passwordEncoder.matches(newPassword, "oldEncodedPassword")).thenReturn(false);
		when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

		passwordResetService.resetPassword(token, newPassword);

		verify(tokenRepository).findByToken(token);
		verify(passwordEncoder).matches(newPassword, "oldEncodedPassword");
		verify(passwordEncoder).encode(newPassword);
		verify(userRepository).save(userCaptor.capture());
		verify(tokenRepository).save(tokenCaptor.capture());

		User savedUser = userCaptor.getValue();
		EmailToken savedToken = tokenCaptor.getValue();

		assertThat(savedUser.getPassword()).isEqualTo(encodedPassword);
		assertThat(savedToken.isConfirmed()).isTrue();
		assertThat(savedToken.getConfirmedAt()).isNotNull();
	}

	@Test
	void resetPassword_ShouldThrowInvalidTokenException_WhenTokenIsInvalid() {
		String token = "invalid-token";
		String newPassword = "newPassword123";

		when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> passwordResetService.resetPassword(token, newPassword))
				.isInstanceOf(InvalidTokenException.class).hasMessageContaining("Invalid password-reset token");

		verify(tokenRepository).findByToken(token);
		verify(passwordEncoder, never()).matches(any(), any());
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
				.user(user).confirmed(false).build();

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

		assertThatThrownBy(() -> passwordResetService.resetPassword(token, newPassword))
				.isInstanceOf(TokenExpiredException.class).hasMessageContaining("password-reset token has expired");

		verify(tokenRepository).findByToken(token);
		verify(passwordEncoder, never()).matches(any(), any());
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
				.confirmed(false).build();

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
		when(passwordEncoder.matches(newPassword, "oldEncodedPassword")).thenReturn(true);

		assertThatThrownBy(() -> passwordResetService.resetPassword(token, newPassword))
				.isInstanceOf(SamePasswordException.class)
				.hasMessageContaining("New password cannot be the same as current password");

		verify(tokenRepository).findByToken(token);
		verify(passwordEncoder).matches(newPassword, "oldEncodedPassword");
		verify(passwordEncoder, never()).encode(any());
		verify(userRepository, never()).save(any());
		verify(tokenRepository, never()).save(any());
	}

	@Test
	void resetPassword_ShouldThrowInvalidTokenException_WhenTokenAlreadyConfirmed() {
		String token = "confirmed-token";
		String newPassword = "newPassword123";

		User user = new User();
		user.setEmail("test@example.com");

		EmailToken resetToken = EmailToken.builder().token(token).expiresAt(LocalDateTime.now().plusHours(1)).user(user)
				.confirmed(true).confirmedAt(LocalDateTime.now().minusHours(1)).build();

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

		assertThatThrownBy(() -> passwordResetService.resetPassword(token, newPassword))
				.isInstanceOf(InvalidTokenException.class).hasMessageContaining("Invalid password-reset token");

		verify(tokenRepository).findByToken(token);
		verify(passwordEncoder, never()).matches(any(), any());
		verify(passwordEncoder, never()).encode(any());
		verify(userRepository, never()).save(any());
		verify(tokenRepository, never()).save(any());
	}

	@Test
	void resetPassword_ShouldNotCheckPasswordMatch_WhenOldPasswordIsNull() {
		String token = "valid-token";
		String newPassword = "newPassword123";
		String encodedPassword = "encodedNewPassword";

		User user = new User();
		user.setId(1L);
		user.setEmail("test@example.com");
		user.setPassword(null);
		user.setEnabled(true);

		EmailToken resetToken = EmailToken.builder().token(token).expiresAt(LocalDateTime.now().plusHours(1)).user(user)
				.confirmed(false).build();

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
		when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

		passwordResetService.resetPassword(token, newPassword);

		verify(tokenRepository).findByToken(token);
		verify(passwordEncoder, never()).matches(any(), any());
		verify(passwordEncoder).encode(newPassword);
		verify(userRepository).save(userCaptor.capture());
		verify(tokenRepository).save(tokenCaptor.capture());

		User savedUser = userCaptor.getValue();
		EmailToken savedToken = tokenCaptor.getValue();

		assertThat(savedUser.getPassword()).isEqualTo(encodedPassword);
		assertThat(savedToken.isConfirmed()).isTrue();
		assertThat(savedToken.getConfirmedAt()).isNotNull();
	}

	@Test
	void resetPassword_ShouldHandleTokenExpirationExactlyNow() {
		String token = "expiring-now-token";
		String newPassword = "newPassword123";

		User user = new User();
		user.setEmail("test@example.com");

		EmailToken resetToken = EmailToken.builder().token(token).expiresAt(LocalDateTime.now()).user(user)
				.confirmed(false).build();

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

		assertThatThrownBy(() -> passwordResetService.resetPassword(token, newPassword))
				.isInstanceOf(TokenExpiredException.class).hasMessageContaining("password-reset token has expired");

		verify(tokenRepository).findByToken(token);
		verify(passwordEncoder, never()).matches(any(), any());
		verify(passwordEncoder, never()).encode(any());
		verify(userRepository, never()).save(any());
		verify(tokenRepository, never()).save(any());
	}

	@Test
	void resetPassword_ShouldEncodePasswordEvenWhenOldPasswordIsBlank() {
		String token = "valid-token";
		String newPassword = "newPassword123";
		String encodedPassword = "encodedNewPassword";

		User user = new User();
		user.setId(1L);
		user.setEmail("test@example.com");
		user.setPassword("");
		user.setEnabled(true);

		EmailToken resetToken = EmailToken.builder().token(token).expiresAt(LocalDateTime.now().plusHours(1)).user(user)
				.confirmed(false).build();

		when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
		when(passwordEncoder.matches(newPassword, "")).thenReturn(false);
		when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

		passwordResetService.resetPassword(token, newPassword);

		verify(tokenRepository).findByToken(token);
		verify(passwordEncoder).matches(newPassword, "");
		verify(passwordEncoder).encode(newPassword);
		verify(userRepository).save(userCaptor.capture());

		User savedUser = userCaptor.getValue();
		assertThat(savedUser.getPassword()).isEqualTo(encodedPassword);
	}
}