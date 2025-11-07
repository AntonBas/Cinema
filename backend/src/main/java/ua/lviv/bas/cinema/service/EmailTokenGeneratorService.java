package ua.lviv.bas.cinema.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.domain.EmailToken;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TokenType;
import ua.lviv.bas.cinema.exception.UserNotFoundException;
import ua.lviv.bas.cinema.repository.EmailTokenRepository;
import ua.lviv.bas.cinema.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTokenGeneratorService {

	private final EmailTokenRepository tokenRepository;
	private final UserRepository userRepository;
	private final EmailService emailService;

	@Transactional
	public String generateVerificationToken(String email) {
		return generateToken(email, TokenType.VERIFICATION, null);
	}

	@Transactional
	public String generatePasswordResetToken(String email) {
		return generateToken(email, TokenType.PASSWORD_RESET, null);
	}

	@Transactional
	public String generateEmailChangeToken(String email, String newEmail) {
		return generateToken(email, TokenType.EMAIL_CHANGE, newEmail);
	}

	private String generateToken(String email, TokenType tokenType, String newEmail) {
		log.info("Generating {} token for email: {}", tokenType, email);

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

		tokenRepository.deleteByUserAndType(user, tokenType);

		String token = UUID.randomUUID().toString();
		EmailToken emailToken = buildEmailToken(token, user, tokenType, newEmail);

		tokenRepository.save(emailToken);
		log.info("Saved new {} token for user: {}", tokenType, email);

		sendEmailByType(email, token, tokenType, newEmail);

		return token;
	}

	private EmailToken buildEmailToken(String token, User user, TokenType tokenType, String newEmail) {
		EmailToken.EmailTokenBuilder builder = EmailToken.builder().token(token).user(user)
				.createdAt(LocalDateTime.now()).type(tokenType);

		switch (tokenType) {
		case VERIFICATION, PASSWORD_RESET -> builder.expiresAt(LocalDateTime.now().plusMinutes(10));
		case EMAIL_CHANGE -> builder.expiresAt(LocalDateTime.now().plusHours(24));
		}

		if (tokenType == TokenType.EMAIL_CHANGE && newEmail != null) {
			builder.newEmail(newEmail);
		}

		return builder.build();
	}

	private void sendEmailByType(String email, String token, TokenType tokenType, String newEmail) {
		switch (tokenType) {
		case VERIFICATION -> emailService.sendVerificationEmail(email, token);
		case PASSWORD_RESET -> emailService.sendPasswordResetEmail(email, token);
		case EMAIL_CHANGE -> emailService.sendEmailChangeConfirmation(newEmail, token);
		default -> log.warn("Unknown token type: {}", tokenType);
		}
	}
}