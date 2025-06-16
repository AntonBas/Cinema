package ua.lviv.bas.cinema.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dao.EmailTokenRepository;
import ua.lviv.bas.cinema.domain.EmailToken;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TokenType;

@Service
@RequiredArgsConstructor
public class EmailTokenGeneratorService {

	private static final Logger logger = LogManager.getLogger(EmailTokenGeneratorService.class);

	private final EmailTokenRepository tokenRepository;
	private final UserService userService;
	private final EmailService emailService;

	@Transactional
	public String generateVerificationToken(String email) {
		logger.info("Generating verification token for email: {}", email);
		User user = userService.findByEmail(email);

		if (user == null) {
			logger.error("No user found with email: {}", email);
			throw new RuntimeException("There is no user with this email address.");
		}

		String token = UUID.randomUUID().toString();
		EmailToken emailToken = new EmailToken();
		emailToken.setToken(token);
		emailToken.setUser(user);
		emailToken.setCreatedAt(LocalDateTime.now());
		emailToken.setExpiresAt(LocalDateTime.now().plusMinutes(10));
		emailToken.setType(TokenType.VERIFICATION);

		tokenRepository.save(emailToken);
		logger.info("Saved new verification token for user with email: {}", email);

		emailService.sendVerificationEmail(email, token);
		logger.info("Sent verification email to {}", email);

		return token;
	}

	@Transactional
	public String generatePasswordResetToken(String email) {
		logger.info("Generating password reset token for email: {}", email);
		User user = userService.findByEmail(email);

		if (user == null) {
			logger.error("No user found with email: {}", email);
			throw new RuntimeException("There is no user with this email address.");
		}

		String token = UUID.randomUUID().toString();
		EmailToken emailToken = new EmailToken();
		emailToken.setToken(token);
		emailToken.setUser(user);
		emailToken.setCreatedAt(LocalDateTime.now());
		emailToken.setExpiresAt(LocalDateTime.now().plusMinutes(10));
		emailToken.setType(TokenType.PASSWORD_RESET);

		tokenRepository.save(emailToken);
		logger.info("Saved new password reset token for user with email: {}", email);

		emailService.sendPasswordResetEmail(email, token);
		logger.info("Sent password reset email to {}", email);
		return token;
	}
}
