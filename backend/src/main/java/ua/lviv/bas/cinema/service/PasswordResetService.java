package ua.lviv.bas.cinema.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.domain.EmailToken;
import ua.lviv.bas.cinema.repository.EmailTokenRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

	private final EmailTokenGeneratorService tokenGeneratorService;
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final EmailTokenRepository tokenRepository;

	public void requestPasswordReset(String email) {
		log.info("Password reset requested for email: {}", email);
		userService.findByEmail(email);
		tokenGeneratorService.generatePasswordResetToken(email);
		log.info("Password reset token generated for: {}", email);
	}

	@Transactional
	public void resetPassword(String token, String newPassword) {
		log.info("Attempting to reset password with token: {}", token);

		if (newPassword.length() < 6) {
			throw new RuntimeException("Password must be at least 6 characters");

		}
		EmailToken resetToken = tokenRepository.findByToken(token).orElseThrow(() -> {
			log.error("Invalid password reset token: {}", token);
			return new RuntimeException("Invalid token");
		});

		if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
			log.error("Expired password reset token: {}", token);
			throw new RuntimeException("Token expired");
		}

		var user = resetToken.getUser();
		user.setPassword(passwordEncoder.encode(newPassword));
		tokenRepository.delete(resetToken);

		log.info("Password updated successfully for user: {}", user.getEmail());
	}
}