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
		return generateToken(email, TokenType.VERIFICATION);
	}

	@Transactional
	public String generatePasswordResetToken(String email) {
		return generateToken(email, TokenType.PASSWORD_RESET);
	}

	private String generateToken(String email, TokenType tokenType) {
		logger.info("Generating {} token for email: {}", tokenType, email);
		User user = userService.findByEmail(email);
		if (user == null) {
			logger.error("No user found with email: {}", email);
			throw new RuntimeException("There is no user with this email address.");
		}

		tokenRepository.findByUserEmailAndType(email, tokenType).ifPresent(existingToken -> {
			if (!existingToken.isConfirmed() && existingToken.getExpiresAt().isAfter(LocalDateTime.now())) {
				tokenRepository.delete(existingToken);
				logger.info("Deleted existing active {} token for user {}", tokenType, email);
			}
		});

		String token = UUID.randomUUID().toString();
		EmailToken emailToken = new EmailToken();
		emailToken.setToken(token);
		emailToken.setUser(user);
		emailToken.setCreatedAt(LocalDateTime.now());
		emailToken.setExpiresAt(LocalDateTime.now().plusMinutes(10));
		emailToken.setType(tokenType);

		tokenRepository.save(emailToken);
		logger.info("Saved new {} token for user with email: {}", tokenType, email);

		if (tokenType == TokenType.VERIFICATION) {
			emailService.sendVerificationEmail(email, token);
		} else if (tokenType == TokenType.PASSWORD_RESET) {
			emailService.sendPasswordResetEmail(email, token);
		}

		logger.info("Sent {} email to {}", tokenType, email);

		return token;
	}
}
