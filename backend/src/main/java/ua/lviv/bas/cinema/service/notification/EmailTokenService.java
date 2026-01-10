package ua.lviv.bas.cinema.service.notification;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.EmailToken;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TokenType;
import ua.lviv.bas.cinema.exception.domain.auth.EmailAlreadyExistsException;
import ua.lviv.bas.cinema.exception.domain.auth.EmailValidationException;
import ua.lviv.bas.cinema.exception.domain.auth.InvalidTokenException;
import ua.lviv.bas.cinema.exception.domain.auth.TokenAlreadyConfirmedException;
import ua.lviv.bas.cinema.exception.domain.auth.TokenExpiredException;
import ua.lviv.bas.cinema.repository.EmailTokenRepository;
import ua.lviv.bas.cinema.repository.UserRepository;
import ua.lviv.bas.cinema.service.user.BonusService;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTokenService {

	private final EmailTokenRepository tokenRepository;
	private final EmailService emailService;
	private final UserRepository userRepository;
	private final BonusService bonusUserService;

	@Transactional
	public String confirmEmail(String token) {
		log.info("Attempting to confirm email with token: {}", token);

		EmailToken emailToken = validateToken(token);

		if (emailToken.getType() != TokenType.VERIFICATION) {
			throw new InvalidTokenException("email-verification");
		}

		User user = emailToken.getUser();
		user.setEnabled(true);
		User updatedUser = userRepository.save(user);

		bonusUserService.findOrCreateBonusCard(updatedUser);

		bonusUserService.awardWelcomeBonus(updatedUser);

		emailToken.setConfirmed(true);
		emailToken.setConfirmedAt(LocalDateTime.now());
		tokenRepository.save(emailToken);

		log.info("Email confirmed successfully for user: {}", user.getEmail());
		return "Email successfully verified! You can now log in.";
	}

	@Transactional
	public User confirmEmailChange(String token) {
		log.info("Attempting to confirm email change with token: {}", token);

		EmailToken emailToken = validateToken(token);

		if (emailToken.getType() != TokenType.EMAIL_CHANGE) {
			throw new InvalidTokenException("email-change");
		}

		if (emailToken.getNewEmail() == null) {
			throw new InvalidTokenException("email-change");
		}

		User user = emailToken.getUser();
		String oldEmail = user.getEmail();
		String newEmail = emailToken.getNewEmail();

		if (oldEmail.equalsIgnoreCase(newEmail)) {
			throw EmailValidationException.sameEmail();
		}

		if (userRepository.findByEmail(newEmail).isPresent()) {
			throw new EmailAlreadyExistsException(newEmail);
		}

		user.setEmail(newEmail);
		User updatedUser = userRepository.save(user);

		emailService.sendEmailChangeNotification(oldEmail, newEmail);

		emailToken.setConfirmed(true);
		emailToken.setConfirmedAt(LocalDateTime.now());
		tokenRepository.save(emailToken);

		log.info("Email changed from {} to {} for user ID: {}", oldEmail, newEmail, user.getId());

		return updatedUser;
	}

	private EmailToken validateToken(String token) {
		EmailToken emailToken = tokenRepository.findByToken(token)
				.orElseThrow(() -> new InvalidTokenException("email-verification"));

		if (emailToken.isConfirmed()) {
			throw new TokenAlreadyConfirmedException("email-verification");
		}

		if (emailToken.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new TokenExpiredException("email-verification");
		}

		return emailToken;
	}
}