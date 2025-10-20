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
		verify(tokenRepository).deleteByUserAndTypeAndExpiresAtBefore(eq(user), eq(TokenType.VERIFICATION),
				any(LocalDateTime.class));
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
		verify(tokenRepository).deleteByUserAndTypeAndExpiresAtBefore(eq(user), eq(TokenType.PASSWORD_RESET),
				any(LocalDateTime.class));
		verify(tokenRepository).save(any(EmailToken.class));
		verify(emailService).sendPasswordResetEmail(email, token);
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
	void generateToken_ShouldCleanExpiredTokens_WhenGeneratingNewToken() {
		String email = "test@example.com";
		User user = User.builder().email(email).build();

		when(userService.findByEmail(email)).thenReturn(user);
		when(tokenRepository.save(any(EmailToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

		tokenGeneratorService.generateVerificationToken(email);

		verify(tokenRepository).deleteByUserAndTypeAndExpiresAtBefore(eq(user), eq(TokenType.VERIFICATION),
				any(LocalDateTime.class));
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
}