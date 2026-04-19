package ua.lviv.bas.cinema.service.user;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.token.EmailToken;
import ua.lviv.bas.cinema.domain.token.TokenType;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.exception.domain.auth.InvalidTokenException;
import ua.lviv.bas.cinema.exception.domain.auth.SamePasswordException;
import ua.lviv.bas.cinema.exception.domain.auth.TokenExpiredException;
import ua.lviv.bas.cinema.exception.domain.user.EmailNotVerifiedException;
import ua.lviv.bas.cinema.repository.token.EmailTokenRepository;
import ua.lviv.bas.cinema.repository.user.UserRepository;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;
import ua.lviv.bas.cinema.service.notification.EmailTokenGeneratorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPasswordResetService {

	private final EmailTokenGeneratorService tokenGeneratorService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailTokenRepository tokenRepository;
	private final AuditService auditService;

	@Transactional
	public void requestReset(String email) {
		log.info("Password reset requested for email: {}", email);

		var user = userRepository.findByEmail(email)
				.orElseThrow(() -> new EmailNotVerifiedException("reset password", email));

		if (!user.isEnabled()) {
			throw new EmailNotVerifiedException("reset password", email);
		}

		tokenGeneratorService.generatePasswordResetToken(email);
		log.info("Password reset token generated for: {}", email);
		auditRequestReset(user);
	}

	@Transactional
	public void reset(String token, String newPassword) {
		var resetToken = tokenRepository.findByToken(token)
				.orElseThrow(() -> new InvalidTokenException("password-reset"));

		validateToken(resetToken);

		var user = resetToken.getUser();

		if (passwordEncoder.matches(newPassword, user.getPassword())) {
			throw new SamePasswordException();
		}

		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		resetToken.setConfirmed(true);
		resetToken.setConfirmedAt(LocalDateTime.now());
		tokenRepository.save(resetToken);

		log.info("Password reset successfully for user: {}", user.getEmail());
		auditReset(user);
	}

	private void validateToken(EmailToken token) {
		if (token.getType() != TokenType.PASSWORD_RESET) {
			throw new InvalidTokenException("password-reset");
		}
		if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
			throw new TokenExpiredException("password-reset");
		}
		if (token.isConfirmed()) {
			throw new InvalidTokenException("password-reset");
		}
	}

	private void auditRequestReset(User user) {
		Map<String, Object> details = new HashMap<>();
		details.put("email", user.getEmail());
		details.put("userId", user.getId());
		auditService.logChange("User", user.getId(), user.getEmail(), AuditAction.PASSWORD_RESET_REQUESTED, null,
				details);
	}

	private void auditReset(User user) {
		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("userId", user.getId());
		Map<String, Object> newDetails = new HashMap<>();
		newDetails.put("userId", user.getId());
		newDetails.put("userEmail", user.getEmail());
		auditService.logChange("User", user.getId(), user.getEmail(), AuditAction.PASSWORD_RESET_COMPLETED, oldDetails,
				newDetails);
	}
}