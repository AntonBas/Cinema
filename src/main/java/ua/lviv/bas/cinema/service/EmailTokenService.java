package ua.lviv.bas.cinema.service;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dao.EmailTokenRepository;
import ua.lviv.bas.cinema.domain.EmailToken;
import ua.lviv.bas.cinema.domain.User;

@Service
@RequiredArgsConstructor
public class EmailTokenService {

	private static final Logger logger = LogManager.getLogger(EmailToken.class);

	private final EmailTokenRepository tokenRepository;
	private final UserService userService;

	@Transactional
	public void confirmEmail(String token) {
		logger.info("Attempting to confirm email with token: {}", token);
		EmailToken emailToken = tokenRepository.findByToken(token)
				.orElseThrow(() -> new RuntimeException("Invalid token"));

		if (emailToken.isConfirmed()) {
			throw new RuntimeException("Email already confirmed");
		}

		if (emailToken.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new RuntimeException("Token expired");
		}

		User user = emailToken.getUser();
		user.setEnabled(true);
		userService.updateUser(user);

		emailToken.setConfirmed(true);
		tokenRepository.delete(emailToken);
	}
}
