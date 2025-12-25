package ua.lviv.bas.cinema.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import ua.lviv.bas.cinema.service.common.EmailService;
import ua.lviv.bas.cinema.service.common.EmailTokenGeneratorService;

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

	@Test
	void generateVerificationToken_ShouldGenerateToken_WhenUserExists() {
		String email = "test@example.com";
		User user = new User();
		user.setEmail(email);

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

		String token = tokenGeneratorService.generateVerificationToken(email);

		assertNotNull(token);
		verify(userRepository).findByEmail(email);
		verify(tokenRepository).deleteByUserAndType(user, TokenType.VERIFICATION);
		verify(tokenRepository).save(any(EmailToken.class));
		verify(emailService).sendVerificationEmail(email, token);
	}

	@Test
	void generatePasswordResetToken_ShouldGenerateToken_WhenUserExists() {
		String email = "test@example.com";
		User user = new User();
		user.setEmail(email);

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

		String token = tokenGeneratorService.generatePasswordResetToken(email);

		assertNotNull(token);
		verify(userRepository).findByEmail(email);
		verify(tokenRepository).deleteByUserAndType(user, TokenType.PASSWORD_RESET);
		verify(tokenRepository).save(any(EmailToken.class));
		verify(emailService).sendPasswordResetEmail(email, token);
	}

	@Test
	void generateEmailChangeToken_ShouldGenerateToken_WhenUserExists() {
		String email = "old@example.com";
		String newEmail = "new@example.com";
		User user = new User();
		user.setEmail(email);

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

		String token = tokenGeneratorService.generateEmailChangeToken(email, newEmail);

		assertNotNull(token);
		verify(userRepository).findByEmail(email);
		verify(tokenRepository).deleteByUserAndType(user, TokenType.EMAIL_CHANGE);
		verify(tokenRepository).save(any(EmailToken.class));
		verify(emailService).sendEmailChangeConfirmation(newEmail, token);
	}

	@Test
	void generateToken_ShouldThrowException_WhenUserNotFound() {
		String email = "nonexistent@example.com";

		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		UserNotFoundException exception = assertThrows(UserNotFoundException.class,
				() -> tokenGeneratorService.generateVerificationToken(email));

		assertNotNull(exception);
		verify(userRepository).findByEmail(email);
		verifyNoInteractions(tokenRepository, emailService);
	}

	@Test
	void generateToken_ShouldCleanExistingTokens_WhenGeneratingNewToken() {
		String email = "test@example.com";
		User user = new User();
		user.setEmail(email);

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

		tokenGeneratorService.generateVerificationToken(email);

		verify(tokenRepository).deleteByUserAndType(user, TokenType.VERIFICATION);
	}

	@Test
	void generateEmailChangeToken_ShouldIncludeNewEmailInToken() {
		String email = "old@example.com";
		String newEmail = "new@example.com";
		User user = new User();
		user.setEmail(email);

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

		String token = tokenGeneratorService.generateEmailChangeToken(email, newEmail);

		assertNotNull(token);
		verify(emailService).sendEmailChangeConfirmation(newEmail, token);
	}

	@Test
	void generateToken_ShouldSetCreatedAtTimestamp() {
		String email = "test@example.com";
		User user = new User();
		user.setEmail(email);

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

		tokenGeneratorService.generateVerificationToken(email);

		verify(tokenRepository).save(any(EmailToken.class));
	}
}