package ua.lviv.bas.cinema.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.domain.EmailToken;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TokenType;
import ua.lviv.bas.cinema.exception.UserNotFoundException;
import ua.lviv.bas.cinema.repository.EmailTokenRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailTokenGeneratorServiceTest {

	@Mock
	private EmailTokenRepository tokenRepository;

	@Mock
	private UserService userService;

	@Mock
	private EmailService emailService;

	@InjectMocks
	private EmailTokenGeneratorService tokenGeneratorService;

	@Test
	void generateVerificationToken_ShouldGenerateToken_WhenUserExists() {
		String email = "test@example.com";
		User user = User.builder().email(email).build();

		when(userService.findByEmail(email)).thenReturn(user);
		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

		String token = tokenGeneratorService.generateVerificationToken(email);

		assertNotNull(token);
		verify(userService).findByEmail(email);
		verify(tokenRepository).deleteByUserAndType(user, TokenType.VERIFICATION);
		verify(tokenRepository).save(any(EmailToken.class));
		verify(emailService).sendVerificationEmail(email, token);
	}

	@Test
	void generatePasswordResetToken_ShouldGenerateToken_WhenUserExists() {
		String email = "test@example.com";
		User user = User.builder().email(email).build();

		when(userService.findByEmail(email)).thenReturn(user);
		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

		String token = tokenGeneratorService.generatePasswordResetToken(email);

		assertNotNull(token);
		verify(userService).findByEmail(email);
		verify(tokenRepository).deleteByUserAndType(user, TokenType.PASSWORD_RESET);
		verify(tokenRepository).save(any(EmailToken.class));
		verify(emailService).sendPasswordResetEmail(email, token);
	}

	@Test
	void generateEmailChangeToken_ShouldGenerateToken_WhenUserExists() {
		String email = "old@example.com";
		String newEmail = "new@example.com";
		User user = User.builder().email(email).build();

		when(userService.findByEmail(email)).thenReturn(user);
		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

		String token = tokenGeneratorService.generateEmailChangeToken(email, newEmail);

		assertNotNull(token);
		verify(userService).findByEmail(email);
		verify(tokenRepository).deleteByUserAndType(user, TokenType.EMAIL_CHANGE);
		verify(tokenRepository).save(any(EmailToken.class));
		verify(emailService).sendEmailChangeConfirmation(newEmail, token);
	}

	@Test
	void generateToken_ShouldThrowException_WhenUserNotFound() {
		String email = "nonexistent@example.com";

		when(userService.findByEmail(email)).thenThrow(new UserNotFoundException("User not found"));

		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> tokenGeneratorService.generateVerificationToken(email));

		assertEquals("User not found with email: " + email, exception.getMessage());
		verify(userService).findByEmail(email);
		verifyNoInteractions(tokenRepository, emailService);
	}

	@Test
	void generateToken_ShouldCleanExistingTokens_WhenGeneratingNewToken() {
		String email = "test@example.com";
		User user = User.builder().email(email).build();

		when(userService.findByEmail(email)).thenReturn(user);
		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

		tokenGeneratorService.generateVerificationToken(email);

		verify(tokenRepository).deleteByUserAndType(user, TokenType.VERIFICATION);
	}

	@Test
	void generateToken_ShouldCreateTokenWithCorrectExpiration() {
		String email = "test@example.com";
		User user = User.builder().email(email).build();

		when(userService.findByEmail(email)).thenReturn(user);
		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> {
			EmailToken savedToken = invocation.getArgument(0);
			assertTrue(savedToken.getExpiresAt().isAfter(LocalDateTime.now()));
			assertEquals(TokenType.VERIFICATION, savedToken.getType());
			assertEquals(user, savedToken.getUser());
			return savedToken;
		});

		String token = tokenGeneratorService.generateVerificationToken(email);

		assertNotNull(token);
		verify(tokenRepository).save(any(EmailToken.class));
	}

	@Test
	void generateToken_ShouldSetCorrectExpirationTimes() {
		String email = "test@example.com";
		User user = User.builder().email(email).build();

		when(userService.findByEmail(email)).thenReturn(user);

		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> {
			EmailToken savedToken = invocation.getArgument(0);
			assertTrue(savedToken.getExpiresAt().isAfter(LocalDateTime.now().plusMinutes(9)));
			assertTrue(savedToken.getExpiresAt().isBefore(LocalDateTime.now().plusMinutes(11)));
			return savedToken;
		});

		tokenGeneratorService.generateVerificationToken(email);

		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> {
			EmailToken savedToken = invocation.getArgument(0);
			assertTrue(savedToken.getExpiresAt().isAfter(LocalDateTime.now().plusMinutes(9)));
			assertTrue(savedToken.getExpiresAt().isBefore(LocalDateTime.now().plusMinutes(11)));
			return savedToken;
		});

		tokenGeneratorService.generatePasswordResetToken(email);

		String newEmail = "new@example.com";
		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> {
			EmailToken savedToken = invocation.getArgument(0);
			assertTrue(savedToken.getExpiresAt().isAfter(LocalDateTime.now().plusHours(23)));
			assertTrue(savedToken.getExpiresAt().isBefore(LocalDateTime.now().plusHours(25)));
			assertEquals(newEmail, savedToken.getNewEmail());
			return savedToken;
		});

		tokenGeneratorService.generateEmailChangeToken(email, newEmail);
	}

	@Test
	void generateToken_ShouldGenerateValidUUID() {
		String email = "test@example.com";
		User user = User.builder().email(email).build();

		when(userService.findByEmail(email)).thenReturn(user);
		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

		String token = tokenGeneratorService.generateVerificationToken(email);

		assertNotNull(token);
		assertDoesNotThrow(() -> UUID.fromString(token));
	}

	@Test
	void generateEmailChangeToken_ShouldIncludeNewEmailInToken() {
		String email = "old@example.com";
		String newEmail = "new@example.com";
		User user = User.builder().email(email).build();

		when(userService.findByEmail(email)).thenReturn(user);
		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> {
			EmailToken savedToken = invocation.getArgument(0);
			assertEquals(newEmail, savedToken.getNewEmail());
			assertEquals(TokenType.EMAIL_CHANGE, savedToken.getType());
			return savedToken;
		});

		String token = tokenGeneratorService.generateEmailChangeToken(email, newEmail);

		assertNotNull(token);
		verify(emailService).sendEmailChangeConfirmation(newEmail, token);
	}
}