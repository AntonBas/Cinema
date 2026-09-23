package ua.lviv.bas.cinema.user.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.user.repository.EmailTokenRepository;

import java.time.Instant;

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
        when(tokenRepository.deleteByExpiresAtBefore(any(Instant.class))).thenReturn(3);

        emailTokenCleanupScheduler.cleanupExpiredTokens();

        verify(tokenRepository).deleteByExpiresAtBefore(any(Instant.class));
    }

    @Test
    void cleanupExpiredTokensWhenNoneFoundShouldStillCallRepository() {
        when(tokenRepository.deleteByExpiresAtBefore(any(Instant.class))).thenReturn(0);

        emailTokenCleanupScheduler.cleanupExpiredTokens();

        verify(tokenRepository).deleteByExpiresAtBefore(any(Instant.class));
    }

    @Test
    void cleanupOldConfirmedTokensShouldDeleteOldConfirmedTokens() {
        when(tokenRepository.deleteByConfirmedTrueAndConfirmedAtBefore(any(Instant.class))).thenReturn(2);

        emailTokenCleanupScheduler.cleanupOldConfirmedTokens();

        verify(tokenRepository).deleteByConfirmedTrueAndConfirmedAtBefore(any(Instant.class));
    }
}
