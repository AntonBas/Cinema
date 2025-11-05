package ua.lviv.bas.cinema.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.EmailToken;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.repository.EmailTokenRepository;
import ua.lviv.bas.cinema.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTokenService {

	private final EmailTokenRepository tokenRepository;
	private final EmailService emailService;
	private final UserService userService;
	private final UserRepository userRepository;

	@Transactional
	public String confirmEmail(String token) {
		log.info("Attempting to confirm email with token: {}", token);
		EmailToken emailToken = tokenRepository.findByToken(token)
				.orElseThrow(() -> new RuntimeException("Invalid token"));

		if (emailToken.isConfirmed()) {
			return "Email already confirmed";
		}

		if (emailToken.getExpiresAt().isBefore(LocalDateTime.now())) {
			return "Token expired";
		}

		User user = emailToken.getUser();
		user.setEnabled(true);
		userService.updateUser(user);

		emailToken.setConfirmed(true);
		emailToken.setConfirmedAt(LocalDateTime.now());

		log.info("Email confirmed successfully for user: {}", user.getEmail());
		return "Email successfully verified! You can now log in.";
	}

	@Transactional
	public User confirmEmailChange(String token) {
		log.info("Attempting to confirm email change with token: {}", token);
		EmailToken emailToken = tokenRepository.findByToken(token)
				.orElseThrow(() -> new RuntimeException("Invalid token"));

		User user = emailToken.getUser();
		String oldEmail = user.getEmail();
		String newEmail = emailToken.getNewEmail();

		if (userService.existsByEmail(newEmail)) {
			throw new RuntimeException("Email is already registered: " + newEmail);
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
}