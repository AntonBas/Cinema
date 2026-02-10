package ua.lviv.bas.cinema.service.notification;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.EmailToken;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TokenType;
import ua.lviv.bas.cinema.exception.domain.user.UserNotFoundException;
import ua.lviv.bas.cinema.repository.EmailTokenRepository;
import ua.lviv.bas.cinema.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class EmailTokenGeneratorServiceTest {

	@Mock
	private EmailTokenRepository tokenRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private EmailService emailService;

	@InjectMocks
	private EmailTokenGeneratorService tokenGeneratorService;

	private final String USER_EMAIL = "test@example.com";
	private final String NEW_EMAIL = "new@example.com";

	@Test
	void generateVerificationToken_Success() {
		User user = createUser();

		when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

		tokenGeneratorService.generateVerificationToken(USER_EMAIL);

		verify(tokenRepository).deleteByUserAndType(user, TokenType.VERIFICATION);
		verify(tokenRepository).save(any(EmailToken.class));
		verify(emailService).sendVerificationEmail(eq(USER_EMAIL), any());
	}

	@Test
	void generatePasswordResetToken_Success() {
		User user = createUser();

		when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

		tokenGeneratorService.generatePasswordResetToken(USER_EMAIL);

		verify(tokenRepository).deleteByUserAndType(user, TokenType.PASSWORD_RESET);
		verify(tokenRepository).save(any(EmailToken.class));
		verify(emailService).sendPasswordResetEmail(eq(USER_EMAIL), any());
	}

	@Test
	void generateEmailChangeToken_Success() {
		User user = createUser();

		when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

		tokenGeneratorService.generateEmailChangeToken(USER_EMAIL, NEW_EMAIL);

		verify(tokenRepository).deleteByUserAndType(user, TokenType.EMAIL_CHANGE);
		verify(tokenRepository).save(any(EmailToken.class));
		verify(emailService).sendEmailChangeConfirmation(eq(NEW_EMAIL), any());
	}

	@Test
	void generateEmailChangeToken_NullNewEmail_ThrowsException() {
		assertThatThrownBy(() -> tokenGeneratorService.generateEmailChangeToken(USER_EMAIL, null))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void generateToken_UserNotFound_ThrowsException() {
		when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> tokenGeneratorService.generateVerificationToken(USER_EMAIL))
				.isInstanceOf(UserNotFoundException.class);
	}

	private User createUser() {
		User user = new User();
		user.setId(1L);
		user.setEmail(USER_EMAIL);
		return user;
	}
}