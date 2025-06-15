package ua.lviv.bas.cinema.service;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ua.lviv.bas.cinema.dao.EmailTokenRepository;
import ua.lviv.bas.cinema.domain.EmailToken;
import ua.lviv.bas.cinema.domain.User;

@Service
public class EmailTokenService {

	private static final Logger logger = LogManager.getLogger(EmailToken.class);

	@Autowired
	private EmailTokenRepository tokenRepository;

	@Autowired
	private UserService userService;

	@Transactional
	public void confirmEmail(String token) {
		logger.info("Attempting to confirm email with token: {}", token);
		EmailToken emailToken = tokenRepository.findByToken(token)
				.orElseThrow(() -> new RuntimeException("Invalid token"));

		if (emailToken.isConfirmed()) {
			throw new RuntimeException("Email вже підтверджено");
		}

		if (emailToken.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new RuntimeException("Токен прострочений");
		}

		User user = emailToken.getUser();
		user.setEnabled(true);
		userService.updateUser(user);

		emailToken.setConfirmed(true);
		tokenRepository.save(emailToken);
	}
}
