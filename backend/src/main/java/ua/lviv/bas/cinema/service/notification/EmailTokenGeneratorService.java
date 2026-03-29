package ua.lviv.bas.cinema.service.notification;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.token.EmailToken;
import ua.lviv.bas.cinema.domain.token.TokenType;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.exception.domain.user.UserNotFoundException;
import ua.lviv.bas.cinema.repository.token.EmailTokenRepository;
import ua.lviv.bas.cinema.repository.user.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTokenGeneratorService {

	private final EmailTokenRepository tokenRepository;
	private final UserRepository userRepository;
	private final EmailService emailService;

	@Transactional
	public void generateVerificationToken(String email) {
		generateAndSendToken(email, TokenType.VERIFICATION, null);
	}

	@Transactional
	public void generatePasswordResetToken(String email) {
		generateAndSendToken(email, TokenType.PASSWORD_RESET, null);
	}

	@Transactional
	public void generateEmailChangeToken(String email, String newEmail) {
		if (newEmail == null || newEmail.trim().isEmpty()) {
			throw new IllegalArgumentException("New email cannot be null or empty");
		}
		generateAndSendToken(email, TokenType.EMAIL_CHANGE, newEmail);
	}

	private void generateAndSendToken(String email, TokenType tokenType, String newEmail) {
		log.info("Generating {} token for email: {}", tokenType, email);

		User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

		tokenRepository.deleteByUserAndType(user, tokenType);

		String token = UUID.randomUUID().toString();
		EmailToken emailToken = buildEmailToken(token, user, tokenType, newEmail);

		tokenRepository.save(emailToken);
		log.info("Generated new {} token for user: {}", tokenType, email);

		sendEmail(email, token, tokenType, newEmail);
	}

	private EmailToken buildEmailToken(String token, User user, TokenType tokenType, String newEmail) {
		EmailToken.EmailTokenBuilder builder = EmailToken.builder().token(token).user(user).type(tokenType)
				.createdAt(LocalDateTime.now());

		LocalDateTime expiresAt = switch (tokenType) {
		case VERIFICATION, PASSWORD_RESET -> LocalDateTime.now().plusMinutes(10);
		case EMAIL_CHANGE -> LocalDateTime.now().plusHours(24);
		};
		builder.expiresAt(expiresAt);

		if (tokenType == TokenType.EMAIL_CHANGE) {
			builder.newEmail(newEmail);
		}

		return builder.build();
	}

	private void sendEmail(String email, String token, TokenType tokenType, String newEmail) {
		switch (tokenType) {
		case VERIFICATION -> emailService.sendVerificationEmail(email, token);
		case PASSWORD_RESET -> emailService.sendPasswordResetEmail(email, token);
		case EMAIL_CHANGE -> {
			if (newEmail != null) {
				emailService.sendEmailChangeConfirmation(newEmail, token);
			} else {
				log.error("New email is null for EMAIL_CHANGE token type");
			}
		}
		default -> log.warn("Unknown token type: {}", tokenType);
		}
	}
}