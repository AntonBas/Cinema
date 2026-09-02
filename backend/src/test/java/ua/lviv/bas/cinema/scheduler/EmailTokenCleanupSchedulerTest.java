package ua.lviv.bas.cinema.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.repository.token.EmailTokenRepository;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailTokenCleanupSchedulerTest {

    @Mock
    private EmailTokenRepository tokenRepository;

    @InjectMocks
    private EmailTokenCleanupScheduler emailTokenCleanupScheduler;

    @Test
    void cleanupExpiredTokensShouldDeleteExpiredTokens() {
        when(tokenRepository.deleteByExpiresAtBefore(any(LocalDateTime.class))).thenReturn(3);

        emailTokenCleanupScheduler.cleanupExpiredTokens();

        verify(tokenRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }

    @Test
    void cleanupExpiredTokensWhenNoneFoundShouldStillCallRepository() {
        when(tokenRepository.deleteByExpiresAtBefore(any(LocalDateTime.class))).thenReturn(0);

        emailTokenCleanupScheduler.cleanupExpiredTokens();

        verify(tokenRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }

    @Test
    void cleanupOldConfirmedTokensShouldDeleteOldConfirmedTokens() {
        when(tokenRepository.deleteByConfirmedTrueAndConfirmedAtBefore(any(LocalDateTime.class))).thenReturn(2);

        emailTokenCleanupScheduler.cleanupOldConfirmedTokens();

        verify(tokenRepository).deleteByConfirmedTrueAndConfirmedAtBefore(any(LocalDateTime.class));
    }
}
