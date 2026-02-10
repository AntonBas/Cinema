package ua.lviv.bas.cinema.service.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import ua.lviv.bas.cinema.domain.enums.TokenType;
import ua.lviv.bas.cinema.exception.domain.auth.EmailAlreadyExistsException;
import ua.lviv.bas.cinema.exception.domain.auth.EmailValidationException;
import ua.lviv.bas.cinema.exception.domain.auth.InvalidTokenException;
import ua.lviv.bas.cinema.exception.domain.auth.TokenAlreadyConfirmedException;
import ua.lviv.bas.cinema.exception.domain.auth.TokenExpiredException;
import ua.lviv.bas.cinema.repository.EmailTokenRepository;
import ua.lviv.bas.cinema.repository.UserRepository;
import ua.lviv.bas.cinema.service.user.BonusService;

@ExtendWith(MockitoExtension.class)
class EmailTokenServiceTest {

	@Mock
	private EmailTokenRepository tokenRepository;

	@Mock
	private EmailService emailService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private BonusService bonusUserService;

	@InjectMocks
	private EmailTokenService emailTokenService;

	private final String TOKEN = "test-token";
	private final String USER_EMAIL = "user@example.com";
	private final String NEW_EMAIL = "new@example.com";

	@Test
	void confirmEmail_Success() {
		User user = createUser();
		user.setEnabled(false);

		EmailToken emailToken = EmailToken.builder().token(TOKEN).type(TokenType.VERIFICATION).confirmed(false)
				.expiresAt(LocalDateTime.now().plusHours(1)).user(user).build();

		when(tokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(emailToken));
		when(userRepository.save(user)).thenReturn(user);

		String result = emailTokenService.confirmEmail(TOKEN);

		assertThat(result).contains("successfully");
		assertThat(user.isEnabled()).isTrue();
		assertThat(emailToken.isConfirmed()).isTrue();
		verify(userRepository).save(user);
		verify(bonusUserService).getOrCreateCard(user);
		verify(bonusUserService).awardWelcomeBonus(user);
	}

	@Test
	void confirmEmail_TokenNotFound_ThrowsException() {
		when(tokenRepository.findByToken(TOKEN)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> emailTokenService.confirmEmail(TOKEN)).isInstanceOf(InvalidTokenException.class);
	}

	@Test
	void confirmEmail_TokenAlreadyConfirmed_ThrowsException() {
		User user = createUser();
		EmailToken emailToken = EmailToken.builder().token(TOKEN).type(TokenType.VERIFICATION).confirmed(true)
				.expiresAt(LocalDateTime.now().plusHours(1)).user(user).build();

		when(tokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(emailToken));

		assertThatThrownBy(() -> emailTokenService.confirmEmail(TOKEN))
				.isInstanceOf(TokenAlreadyConfirmedException.class);
	}

	@Test
	void confirmEmail_TokenExpired_ThrowsException() {
		User user = createUser();
		EmailToken emailToken = EmailToken.builder().token(TOKEN).type(TokenType.VERIFICATION).confirmed(false)
				.expiresAt(LocalDateTime.now().minusHours(1)).user(user).build();

		when(tokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(emailToken));

		assertThatThrownBy(() -> emailTokenService.confirmEmail(TOKEN)).isInstanceOf(TokenExpiredException.class);
	}

	@Test
	void confirmEmail_WrongTokenType_ThrowsException() {
		User user = createUser();
		EmailToken emailToken = EmailToken.builder().token(TOKEN).type(TokenType.EMAIL_CHANGE).confirmed(false)
				.expiresAt(LocalDateTime.now().plusHours(1)).user(user).build();

		when(tokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(emailToken));

		assertThatThrownBy(() -> emailTokenService.confirmEmail(TOKEN)).isInstanceOf(InvalidTokenException.class);
	}

	@Test
	void confirmEmailChange_Success() {
		User user = createUser();
		EmailToken emailToken = EmailToken.builder().token(TOKEN).type(TokenType.EMAIL_CHANGE).confirmed(false)
				.expiresAt(LocalDateTime.now().plusHours(1)).user(user).newEmail(NEW_EMAIL).build();

		when(tokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(emailToken));
		when(userRepository.findByEmail(NEW_EMAIL)).thenReturn(Optional.empty());
		when(userRepository.save(user)).thenReturn(user);

		User result = emailTokenService.confirmEmailChange(TOKEN);

		assertThat(result).isEqualTo(user);
		assertThat(result.getEmail()).isEqualTo(NEW_EMAIL);
		assertThat(emailToken.isConfirmed()).isTrue();
		verify(emailService).sendEmailChangeNotification(USER_EMAIL, NEW_EMAIL);
	}

	@Test
	void confirmEmailChange_NewEmailAlreadyExists_ThrowsException() {
		User user = createUser();
		EmailToken emailToken = EmailToken.builder().token(TOKEN).type(TokenType.EMAIL_CHANGE).confirmed(false)
				.expiresAt(LocalDateTime.now().plusHours(1)).user(user).newEmail(NEW_EMAIL).build();

		when(tokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(emailToken));
		when(userRepository.findByEmail(NEW_EMAIL)).thenReturn(Optional.of(new User()));

		assertThatThrownBy(() -> emailTokenService.confirmEmailChange(TOKEN))
				.isInstanceOf(EmailAlreadyExistsException.class);
	}

	@Test
	void confirmEmailChange_SameEmail_ThrowsException() {
		User user = createUser();
		EmailToken emailToken = EmailToken.builder().token(TOKEN).type(TokenType.EMAIL_CHANGE).confirmed(false)
				.expiresAt(LocalDateTime.now().plusHours(1)).user(user).newEmail(USER_EMAIL).build();

		when(tokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(emailToken));

		assertThatThrownBy(() -> emailTokenService.confirmEmailChange(TOKEN))
				.isInstanceOf(EmailValidationException.class);
	}

	private User createUser() {
		User user = new User();
		user.setId(1L);
		user.setEmail(USER_EMAIL);
		user.setEnabled(true);
		return user;
	}
}