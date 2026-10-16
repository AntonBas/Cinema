package ua.lviv.bas.cinema.user.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.audit.AuditAction;
import ua.lviv.bas.cinema.config.security.CustomUserDetailsService;
import ua.lviv.bas.cinema.notification.EmailService;
import ua.lviv.bas.cinema.user.User;
import ua.lviv.bas.cinema.user.exception.InvalidTokenException;
import ua.lviv.bas.cinema.user.exception.SamePasswordException;
import ua.lviv.bas.cinema.user.exception.TokenExpiredException;
import ua.lviv.bas.cinema.user.UserRepository;
import ua.lviv.bas.cinema.audit.AuditDetails;
import ua.lviv.bas.cinema.audit.AuditService;

import java.time.Instant;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPasswordResetService {

    private static final long RESET_COOLDOWN_SECONDS = 60;

    private final EmailTokenGeneratorService tokenGeneratorService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailTokenRepository tokenRepository;
    private final AuditService auditService;
    private final CustomUserDetailsService customUserDetailsService;
    private final EmailService emailService;

    @Transactional
    public void requestReset(String email) {
        var userOpt = userRepository.findByEmailForUpdate(email);
        if (userOpt.isEmpty()) {
            log.info("Password reset requested for unknown email: {}", email);
            return;
        }

        var user = userOpt.get();
        if (!user.isEnabled()) {
            log.info("Password reset requested for blocked account: {}", email);
            return;
        }
        if (!user.isEmailVerified()) {
            sendVerificationInsteadOfReset(user);
            return;
        }

        if (isWithinCooldown(user.getLastPasswordResetSentAt())) {
            log.info("Password reset requested for {} within cooldown, skipping", email);
            return;
        }

        tokenGeneratorService.generatePasswordResetToken(user);
        user.setLastPasswordResetSentAt(Instant.now());
        userRepository.save(user);
        log.info("Password reset token generated for: {}", email);
        auditRequestReset(user);
    }

    private void sendVerificationInsteadOfReset(User user) {
        if (isWithinCooldown(user.getLastVerificationEmailSentAt())) {
            log.info("Password reset requested for unverified account {} within cooldown, skipping", user.getEmail());
            return;
        }
        tokenGeneratorService.generateVerificationToken(user);
        user.setLastVerificationEmailSentAt(Instant.now());
        userRepository.save(user);
        log.info("Password reset requested for unverified account, verification email sent to: {}", user.getEmail());
    }

    private static boolean isWithinCooldown(Instant lastSentAt) {
        return lastSentAt != null
                && Duration.between(Instant.now(), lastSentAt.plusSeconds(RESET_COOLDOWN_SECONDS)).isPositive();
    }

    @Transactional(readOnly = true)
    public void validateResetToken(String token) {
        var resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("password-reset"));
        validateToken(resetToken);
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
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
        customUserDetailsService.evict(user.getEmail());
        emailService.sendPasswordChangedNotification(user.getEmail());

        resetToken.setConfirmed(true);
        resetToken.setConfirmedAt(Instant.now());
        tokenRepository.save(resetToken);

        log.info("Password reset successfully for user: {}", user.getEmail());
        auditReset(user);
    }

    private void validateToken(EmailToken token) {
        if (token.getType() != TokenType.PASSWORD_RESET) {
            throw new InvalidTokenException("password-reset");
        }
        if (Instant.now().isAfter(token.getExpiresAt())) {
            throw new TokenExpiredException("password-reset");
        }
        if (token.isConfirmed()) {
            throw new InvalidTokenException("password-reset");
        }
    }

    private void auditRequestReset(User user) {
        var details = AuditDetails.of().put("email", user.getEmail()).put("userId", user.getId()).build();
        auditService.logChange("User", user.getId(), user.getEmail(), AuditAction.PASSWORD_RESET_REQUESTED, null,
                details);
    }

    private void auditReset(User user) {
        var oldDetails = AuditDetails.of().put("userId", user.getId()).build();
        var newDetails = AuditDetails.of().put("userId", user.getId()).put("userEmail", user.getEmail()).build();
        auditService.logChange("User", user.getId(), user.getEmail(), AuditAction.PASSWORD_RESET_COMPLETED, oldDetails,
                newDetails);
    }
}