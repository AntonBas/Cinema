package ua.lviv.bas.cinema.service.user;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.EmailToken;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.exception.domain.auth.InvalidTokenException;
import ua.lviv.bas.cinema.exception.domain.auth.SamePasswordException;
import ua.lviv.bas.cinema.exception.domain.auth.TokenExpiredException;
import ua.lviv.bas.cinema.exception.domain.user.EmailNotVerifiedException;
import ua.lviv.bas.cinema.repository.EmailTokenRepository;
import ua.lviv.bas.cinema.repository.UserRepository;
import ua.lviv.bas.cinema.service.notification.EmailTokenGeneratorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPasswordResetService {

	private final EmailTokenGeneratorService tokenGeneratorService;
	private final UserService userService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailTokenRepository tokenRepository;

	public void requestPasswordReset(String email) {
		log.info("Password reset requested for email: {}", email);
		User user = userService.getByEmail(email);

		if (!user.isEnabled()) {
			throw new EmailNotVerifiedException("reset password", email);
		}

		tokenGeneratorService.generatePasswordResetToken(email);
		log.info("Password reset token generated for: {}", email);
	}

	@Transactional
	public void resetPassword(String token, String newPassword) {
		EmailToken resetToken = tokenRepository.findByToken(token)
				.orElseThrow(() -> new InvalidTokenException("password-reset"));

		if (LocalDateTime.now().isAfter(resetToken.getExpiresAt())) {
			throw new TokenExpiredException("password-reset");
		}

		if (resetToken.isConfirmed()) {
			throw new InvalidTokenException("password-reset");
		}

		var user = resetToken.getUser();

		if (user.getPassword() != null && passwordEncoder.matches(newPassword, user.getPassword())) {
			throw new SamePasswordException();
		}

		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		resetToken.setConfirmed(true);
		resetToken.setConfirmedAt(LocalDateTime.now());
		tokenRepository.save(resetToken);

		log.info("Password updated successfully for user: {}", user.getEmail());
	}
}