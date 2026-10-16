package ua.lviv.bas.cinema.user.auth;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.notification.EmailService;
import ua.lviv.bas.cinema.user.auth.EmailToken;
import ua.lviv.bas.cinema.user.auth.TokenType;
import ua.lviv.bas.cinema.user.User;
import ua.lviv.bas.cinema.user.auth.EmailTokenRepository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EmailTokenGeneratorServiceTest {

    @Mock
    private EmailTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailTokenGeneratorService tokenGeneratorService;

    private final String USER_EMAIL = "test@example.com";

    @Test
    void generateVerificationToken_Success() {
        User user = createUser();

        tokenGeneratorService.generateVerificationToken(user);

        verify(tokenRepository).deleteByUserAndType(user, TokenType.VERIFICATION);
        verify(tokenRepository).save(any(EmailToken.class));
        verify(emailService).sendVerificationEmail(eq(USER_EMAIL), any(), eq(Duration.ofHours(24)));
    }

    @Test
    void generatePasswordResetToken_Success() {
        User user = createUser();

        tokenGeneratorService.generatePasswordResetToken(user);

        verify(tokenRepository).deleteByUserAndType(user, TokenType.PASSWORD_RESET);
        verify(tokenRepository).save(any(EmailToken.class));
        verify(emailService).sendPasswordResetEmail(eq(USER_EMAIL), any(), eq(Duration.ofMinutes(30)));
    }

    @Test
    void generateEmailChangeToken_Success() {
        User user = createUser();

        String NEW_EMAIL = "new@example.com";
        tokenGeneratorService.generateEmailChangeToken(user, NEW_EMAIL);

        verify(tokenRepository).deleteByUserAndType(user, TokenType.EMAIL_CHANGE);
        verify(tokenRepository).save(any(EmailToken.class));
        verify(emailService).sendEmailChangeConfirmation(eq(NEW_EMAIL), any(), eq(Duration.ofHours(24)));
    }

    @Test
    void generateEmailChangeToken_NullNewEmail_ThrowsException() {
        User user = createUser();

        assertThatThrownBy(() -> tokenGeneratorService.generateEmailChangeToken(user, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail(USER_EMAIL);
        return user;
    }
}
