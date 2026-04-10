package ua.lviv.bas.cinema.service.notification;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.token.EmailToken;
import ua.lviv.bas.cinema.domain.token.TokenType;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.exception.domain.auth.EmailAlreadyExistsException;
import ua.lviv.bas.cinema.exception.domain.auth.EmailValidationException;
import ua.lviv.bas.cinema.exception.domain.auth.InvalidTokenException;
import ua.lviv.bas.cinema.exception.domain.auth.TokenAlreadyConfirmedException;
import ua.lviv.bas.cinema.exception.domain.auth.TokenExpiredException;
import ua.lviv.bas.cinema.repository.token.EmailTokenRepository;
import ua.lviv.bas.cinema.repository.user.UserRepository;
import ua.lviv.bas.cinema.service.bonus.BonusService;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTokenService {

	private final EmailTokenRepository tokenRepository;
	private final EmailService emailService;
	private final UserRepository userRepository;
	private final BonusService bonusService;

	@Transactional
	public String confirmEmail(String token) {
		log.info("Attempting to confirm email with token: {}", token);

		var emailToken = validateToken(token, TokenType.VERIFICATION);
		var user = emailToken.getUser();

		user.setEnabled(true);
		var updatedUser = userRepository.save(user);

		bonusService.getOrCreateCard(updatedUser);
		bonusService.awardWelcomeBonus(updatedUser);

		emailToken.setConfirmed(true);
		emailToken.setConfirmedAt(LocalDateTime.now());
		tokenRepository.save(emailToken);

		log.info("Email confirmed successfully for user: {}", user.getEmail());
		return "Email successfully verified! You can now log in.";
	}

	@Transactional
	public User confirmEmailChange(String token) {
		log.info("Attempting to confirm email change with token: {}", token);

		var emailToken = validateToken(token, TokenType.EMAIL_CHANGE);

		if (emailToken.getNewEmail() == null) {
			throw new InvalidTokenException("email-change");
		}

		var user = emailToken.getUser();
		var oldEmail = user.getEmail();
		var newEmail = emailToken.getNewEmail();

		if (oldEmail.equalsIgnoreCase(newEmail)) {
			throw EmailValidationException.sameEmail();
		}

		if (userRepository.findByEmail(newEmail).isPresent()) {
			throw new EmailAlreadyExistsException(newEmail);
		}

		user.setEmail(newEmail);
		var updatedUser = userRepository.save(user);

		emailService.sendEmailChangeNotification(oldEmail, newEmail);

		emailToken.setConfirmed(true);
		emailToken.setConfirmedAt(LocalDateTime.now());
		tokenRepository.save(emailToken);

		log.info("Email changed from {} to {} for user ID: {}", oldEmail, newEmail, user.getId());

		return updatedUser;
	}

	private EmailToken validateToken(String token, TokenType expectedType) {
		var emailToken = tokenRepository.findByToken(token)
				.orElseThrow(() -> new InvalidTokenException(expectedType.name().toLowerCase()));

		if (emailToken.getType() != expectedType) {
			throw new InvalidTokenException(expectedType.name().toLowerCase());
		}

		if (emailToken.isConfirmed()) {
			throw new TokenAlreadyConfirmedException(expectedType.name().toLowerCase());
		}

		if (emailToken.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new TokenExpiredException(expectedType.name().toLowerCase());
		}

		return emailToken;
	}
}