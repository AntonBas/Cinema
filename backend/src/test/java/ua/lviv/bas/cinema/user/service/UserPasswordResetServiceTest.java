package ua.lviv.bas.cinema.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.lviv.bas.cinema.config.security.CustomUserDetailsService;
import ua.lviv.bas.cinema.notification.EmailService;
import ua.lviv.bas.cinema.user.domain.EmailToken;
import ua.lviv.bas.cinema.user.domain.TokenType;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.exception.domain.auth.InvalidTokenException;
import ua.lviv.bas.cinema.exception.domain.auth.SamePasswordException;
import ua.lviv.bas.cinema.exception.domain.auth.TokenExpiredException;
import ua.lviv.bas.cinema.user.repository.EmailTokenRepository;
import ua.lviv.bas.cinema.user.repository.UserRepository;
import ua.lviv.bas.cinema.audit.service.AuditService;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserPasswordResetServiceTest {

    @Mock
    private EmailTokenGeneratorService tokenGeneratorService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailTokenRepository tokenRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserPasswordResetService userPasswordResetService;

    @BeforeEach
    void setUp() {
        lenient().doNothing().when(auditService).logChange(any(), any(), any(), any(), any(), any());
    }

    @Test
    void requestResetShouldGenerateTokenWhenUserExistsAndEnabled() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setEnabled(true);
        user.setId(1L);

        when(userRepository.findByEmailForUpdate(email)).thenReturn(Optional.of(user));

        userPasswordResetService.requestReset(email);

        verify(tokenGeneratorService).generatePasswordResetToken(user);
        verify(userRepository).save(user);
        assertThat(user.getLastPasswordResetSentAt()).isNotNull();
    }

    @Test
    void requestResetShouldSilentlyReturnWhenUserNotExists() {
        String email = "unknown@example.com";

        when(userRepository.findByEmailForUpdate(email)).thenReturn(Optional.empty());

        userPasswordResetService.requestReset(email);

        verify(tokenGeneratorService, never()).generatePasswordResetToken(any());
    }

    @Test
    void requestResetShouldSilentlyReturnWhenUserNotEnabled() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setEnabled(false);

        when(userRepository.findByEmailForUpdate(email)).thenReturn(Optional.of(user));

        userPasswordResetService.requestReset(email);

        verify(tokenGeneratorService, never()).generatePasswordResetToken(any());
    }

    @Test
    void requestResetShouldSilentlyReturnWhenWithinCooldown() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setEnabled(true);
        user.setLastPasswordResetSentAt(Instant.now().minus(Duration.ofSeconds(10)));

        when(userRepository.findByEmailForUpdate(email)).thenReturn(Optional.of(user));

        userPasswordResetService.requestReset(email);

        verify(tokenGeneratorService, never()).generatePasswordResetToken(any());
    }

    @Test
    void requestResetShouldGenerateTokenWhenCooldownExpired() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setEnabled(true);
        user.setLastPasswordResetSentAt(Instant.now().minus(Duration.ofSeconds(61)));

        when(userRepository.findByEmailForUpdate(email)).thenReturn(Optional.of(user));

        userPasswordResetService.requestReset(email);

        verify(tokenGeneratorService).generatePasswordResetToken(user);
    }

    @Test
    void resetShouldResetPasswordWhenTokenIsValid() {
        String token = "valid-token";
        String newPassword = "newPassword123";
        String encodedPassword = "encodedNewPassword";

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("oldEncodedPassword");
        user.setEnabled(true);
        int originalTokenVersion = user.getTokenVersion();

        EmailToken resetToken = EmailToken.builder().token(token).type(TokenType.PASSWORD_RESET)
                .expiresAt(Instant.now().plus(Duration.ofHours(1))).user(user).confirmed(false).build();

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.matches(newPassword, "oldEncodedPassword")).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any())).thenReturn(user);
        when(tokenRepository.save(any())).thenReturn(resetToken);

        userPasswordResetService.reset(token, newPassword);

        verify(tokenRepository).findByToken(token);
        verify(passwordEncoder).matches(newPassword, "oldEncodedPassword");
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(user);
        verify(tokenRepository).save(resetToken);
        verify(emailService).sendPasswordChangedNotification("test@example.com");
        assertThat(user.getTokenVersion()).isEqualTo(originalTokenVersion + 1);
    }

    @Test
    void resetShouldThrowInvalidTokenExceptionWhenTokenNotFound() {
        String token = "invalid-token";
        String newPassword = "newPassword123";

        when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userPasswordResetService.reset(token, newPassword))
                .isInstanceOf(InvalidTokenException.class);

        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void resetShouldThrowInvalidTokenExceptionWhenTokenTypeWrong() {
        String token = "wrong-type-token";
        String newPassword = "newPassword123";

        User user = new User();
        EmailToken resetToken = EmailToken.builder().token(token).type(TokenType.VERIFICATION)
                .expiresAt(Instant.now().plus(Duration.ofHours(1))).user(user).build();

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        assertThatThrownBy(() -> userPasswordResetService.reset(token, newPassword))
                .isInstanceOf(InvalidTokenException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void resetShouldThrowTokenExpiredExceptionWhenTokenExpired() {
        String token = "expired-token";
        String newPassword = "newPassword123";

        User user = new User();
        EmailToken resetToken = EmailToken.builder().token(token).type(TokenType.PASSWORD_RESET)
                .expiresAt(Instant.now().minus(Duration.ofHours(1))).user(user).confirmed(false).build();

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        assertThatThrownBy(() -> userPasswordResetService.reset(token, newPassword))
                .isInstanceOf(TokenExpiredException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void resetShouldThrowInvalidTokenExceptionWhenTokenAlreadyConfirmed() {
        String token = "confirmed-token";
        String newPassword = "newPassword123";

        User user = new User();
        EmailToken resetToken = EmailToken.builder().token(token).type(TokenType.PASSWORD_RESET)
                .expiresAt(Instant.now().plus(Duration.ofHours(1))).user(user).confirmed(true).build();

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        assertThatThrownBy(() -> userPasswordResetService.reset(token, newPassword))
                .isInstanceOf(InvalidTokenException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void resetShouldThrowSamePasswordExceptionWhenNewPasswordMatchesOld() {
        String token = "valid-token";
        String newPassword = "samePassword";

        User user = new User();
        user.setPassword("oldEncodedPassword");

        EmailToken resetToken = EmailToken.builder().token(token).type(TokenType.PASSWORD_RESET)
                .expiresAt(Instant.now().plus(Duration.ofHours(1))).user(user).confirmed(false).build();

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.matches(newPassword, "oldEncodedPassword")).thenReturn(true);

        assertThatThrownBy(() -> userPasswordResetService.reset(token, newPassword))
                .isInstanceOf(SamePasswordException.class);

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }
}