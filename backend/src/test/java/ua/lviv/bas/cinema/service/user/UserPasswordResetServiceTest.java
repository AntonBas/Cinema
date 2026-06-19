package ua.lviv.bas.cinema.service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
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

import java.time.LocalDateTime;
import java.util.Optional;

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

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        userPasswordResetService.requestReset(email);

        verify(userRepository).findByEmail(email);
        verify(tokenGeneratorService).generatePasswordResetToken(email);
    }

    @Test
    void requestResetShouldThrowExceptionWhenUserNotExists() {
        String email = "test@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userPasswordResetService.requestReset(email))
                .isInstanceOf(EmailNotVerifiedException.class);

        verify(tokenGeneratorService, never()).generatePasswordResetToken(any());
    }

    @Test
    void requestResetShouldThrowExceptionWhenUserNotEnabled() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setEnabled(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userPasswordResetService.requestReset(email))
                .isInstanceOf(EmailNotVerifiedException.class);

        verify(tokenGeneratorService, never()).generatePasswordResetToken(any());
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

        EmailToken resetToken = EmailToken.builder().token(token).type(TokenType.PASSWORD_RESET)
                .expiresAt(LocalDateTime.now().plusHours(1)).user(user).confirmed(false).build();

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
                .expiresAt(LocalDateTime.now().plusHours(1)).user(user).build();

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
                .expiresAt(LocalDateTime.now().minusHours(1)).user(user).confirmed(false).build();

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
                .expiresAt(LocalDateTime.now().plusHours(1)).user(user).confirmed(true).build();

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
                .expiresAt(LocalDateTime.now().plusHours(1)).user(user).confirmed(false).build();

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.matches(newPassword, "oldEncodedPassword")).thenReturn(true);

        assertThatThrownBy(() -> userPasswordResetService.reset(token, newPassword))
                .isInstanceOf(SamePasswordException.class);

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }
}