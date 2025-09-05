package ua.lviv.bas.cinema.service;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dao.EmailTokenRepository;
import ua.lviv.bas.cinema.dao.UserRepository;
import ua.lviv.bas.cinema.domain.EmailToken;
import ua.lviv.bas.cinema.domain.User;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

	private static final Logger logger = LogManager.getLogger(PasswordResetService.class);

	private final EmailTokenGeneratorService tokenGeneratorService;
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final EmailTokenRepository tokenRepository;
	private final UserRepository userRepository;

	public void requestPasswordReset(String email) {
		logger.info("Password reset requested for email: {}", email);
		User user = userService.findByEmail(email);
		tokenGeneratorService.generatePasswordResetToken(user.getEmail());
		logger.info("Password reset token generated and email sent to: {}", email);
	}

	@Transactional
	public void resetPassword(String token, String newPassword) {
		logger.info("Attempting to reset password with token: {}", token);
		EmailToken resetToken = tokenRepository.findByToken(token).orElseThrow(() -> {
			logger.error("Invalid password reset token: {}", token);
			return new RuntimeException("Invalid token");
		});

		if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
			logger.error("Expired password reset token: {}", token);
			throw new RuntimeException("Token expired");
		}

		User user = resetToken.getUser();
		user.setPassword(passwordEncoder.encode(newPassword));
		resetToken.setConfirmed(true);

		userRepository.save(user);
		tokenRepository.delete(resetToken);

		logger.info("Password updated successfully for user: {}", user.getEmail());
	}
}
