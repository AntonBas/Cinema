package ua.lviv.bas.cinema.user.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.config.security.CustomUserDetailsService;
import ua.lviv.bas.cinema.user.User;
import ua.lviv.bas.cinema.user.exception.EmailAlreadyExistsException;
import ua.lviv.bas.cinema.user.exception.EmailValidationException;
import ua.lviv.bas.cinema.user.exception.InvalidTokenException;
import ua.lviv.bas.cinema.user.exception.TokenAlreadyConfirmedException;
import ua.lviv.bas.cinema.user.exception.TokenExpiredException;
import ua.lviv.bas.cinema.user.UserRepository;
import ua.lviv.bas.cinema.bonus.BonusLedgerService;
import ua.lviv.bas.cinema.notification.EmailService;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTokenService {

    private final EmailTokenRepository tokenRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final BonusLedgerService bonusLedgerService;
    private final CustomUserDetailsService customUserDetailsService;

    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public String confirmEmail(String token) {
        var emailToken = validateToken(token, TokenType.VERIFICATION);
        var user = emailToken.getUser();

        user.setEmailVerified(true);
        var updatedUser = userRepository.save(user);
        customUserDetailsService.evict(user.getEmail());

        bonusLedgerService.awardWelcomeBonus(updatedUser);

        emailToken.setConfirmed(true);
        emailToken.setConfirmedAt(Instant.now());
        tokenRepository.save(emailToken);

        log.info("Email confirmed successfully for user: {}", user.getEmail());
        return user.getEmail();
    }

    @CacheEvict(value = "users", allEntries = true)
    @Transactional
    public User confirmEmailChange(String token) {
        var emailToken = validateToken(token, TokenType.EMAIL_CHANGE);

        if (emailToken.getNewEmail() == null) {
            throw new InvalidTokenException("email-change");
        }

        var user = emailToken.getUser();
        var oldEmail = user.getEmail();
        var newEmail = emailToken.getNewEmail();

        if (oldEmail.equalsIgnoreCase(newEmail)) {
            throw EmailValidationException.sameEmail();
        }

        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new EmailAlreadyExistsException(newEmail);
        }

        user.setEmail(newEmail);
        var updatedUser = userRepository.save(user);
        customUserDetailsService.evict(oldEmail);
        customUserDetailsService.evict(newEmail);

        emailService.sendEmailChangeNotification(oldEmail, newEmail);

        emailToken.setConfirmed(true);
        emailToken.setConfirmedAt(Instant.now());
        tokenRepository.save(emailToken);

        log.info("Email changed from {} to {} for user ID: {}", oldEmail, newEmail, user.getId());

        return updatedUser;
    }

    private EmailToken validateToken(String token, TokenType expectedType) {
        var emailToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException(expectedType.name().toLowerCase()));

        if (emailToken.getType() != expectedType) {
            throw new InvalidTokenException(expectedType.name().toLowerCase());
        }

        if (emailToken.isConfirmed()) {
            throw new TokenAlreadyConfirmedException(expectedType.name().toLowerCase());
        }

        if (emailToken.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenExpiredException(expectedType.name().toLowerCase());
        }

        return emailToken;
    }
}
