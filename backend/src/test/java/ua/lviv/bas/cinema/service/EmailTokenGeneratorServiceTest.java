package ua.lviv.bas.cinema.service;

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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
		User user = User.builder().email(email).build();

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
		User user = User.builder().email(email).build();

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
		User user = User.builder().email(email).build();

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
		User user = User.builder().email(email).build();

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

		tokenGeneratorService.generateVerificationToken(email);

		verify(tokenRepository).deleteByUserAndType(user, TokenType.VERIFICATION);
	}

	@Test
	void generateToken_ShouldCreateTokenWithCorrectExpiration() {
		String email = "test@example.com";
		User user = User.builder().email(email).build();

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
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

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> {
			EmailToken savedToken = invocation.getArgument(0);
			LocalDateTime expectedMin = LocalDateTime.now().plusMinutes(9);
			LocalDateTime expectedMax = LocalDateTime.now().plusMinutes(11);
			assertTrue(savedToken.getExpiresAt().isAfter(expectedMin));
			assertTrue(savedToken.getExpiresAt().isBefore(expectedMax));
			return savedToken;
		});

		tokenGeneratorService.generateVerificationToken(email);

		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> {
			EmailToken savedToken = invocation.getArgument(0);
			LocalDateTime expectedMin = LocalDateTime.now().plusMinutes(9);
			LocalDateTime expectedMax = LocalDateTime.now().plusMinutes(11);
			assertTrue(savedToken.getExpiresAt().isAfter(expectedMin));
			assertTrue(savedToken.getExpiresAt().isBefore(expectedMax));
			return savedToken;
		});

		tokenGeneratorService.generatePasswordResetToken(email);

		String newEmail = "new@example.com";
		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> {
			EmailToken savedToken = invocation.getArgument(0);
			LocalDateTime expectedMin = LocalDateTime.now().plusHours(23);
			LocalDateTime expectedMax = LocalDateTime.now().plusHours(25);
			assertTrue(savedToken.getExpiresAt().isAfter(expectedMin));
			assertTrue(savedToken.getExpiresAt().isBefore(expectedMax));
			assertEquals(newEmail, savedToken.getNewEmail());
			return savedToken;
		});

		tokenGeneratorService.generateEmailChangeToken(email, newEmail);
	}

	@Test
	void generateToken_ShouldGenerateValidUUID() {
		String email = "test@example.com";
		User user = User.builder().email(email).build();

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
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

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
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

	@Test
	void generateEmailChangeToken_ShouldNotSetNewEmail_ForOtherTokenTypes() {
		String email = "test@example.com";
		User user = User.builder().email(email).build();

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> {
			EmailToken savedToken = invocation.getArgument(0);
			assertNull(savedToken.getNewEmail());
			return savedToken;
		});

		tokenGeneratorService.generateVerificationToken(email);
		tokenGeneratorService.generatePasswordResetToken(email);
	}

	@Test
	void generateToken_ShouldSetCreatedAtTimestamp() {
		String email = "test@example.com";
		User user = User.builder().email(email).build();

		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> {
			EmailToken savedToken = invocation.getArgument(0);
			assertNotNull(savedToken.getCreatedAt());
			assertTrue(savedToken.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
			assertTrue(savedToken.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(1)));
			return savedToken;
		});

		tokenGeneratorService.generateVerificationToken(email);
	}
}