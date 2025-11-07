package ua.lviv.bas.cinema.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.EmailToken;
import ua.lviv.bas.cinema.exception.InvalidTokenException;
import ua.lviv.bas.cinema.exception.TokenExpiredException;
import ua.lviv.bas.cinema.repository.EmailTokenRepository;
import ua.lviv.bas.cinema.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

	private final EmailTokenGeneratorService tokenGeneratorService;
	private final UserService userService;
	private final UserRepository userRepository;
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
		EmailToken resetToken = tokenRepository.findByToken(token)
				.orElseThrow(() -> new InvalidTokenException("Invalid token"));

		if (resetToken.isExpired()) {
			throw new TokenExpiredException("Token expired");
		}

		var user = resetToken.getUser();
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		tokenRepository.delete(resetToken);
		log.info("Password updated successfully for user: {}", user.getEmail());
	}

}