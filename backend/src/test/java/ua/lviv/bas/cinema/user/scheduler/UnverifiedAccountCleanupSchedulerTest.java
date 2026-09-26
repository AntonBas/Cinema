package ua.lviv.bas.cinema.user.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ua.lviv.bas.cinema.user.repository.EmailTokenRepository;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnverifiedAccountCleanupSchedulerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailTokenRepository tokenRepository;

    @InjectMocks
    private UnverifiedAccountCleanupScheduler scheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduler, "gracePeriodHours", 48);
    }

    @Test
    void cleanupUnverifiedAccountsShouldDeleteTokensBeforeAccounts() {
        when(tokenRepository.deleteAllByUnverifiedUserCreatedDateBefore(any(Instant.class))).thenReturn(2);
        when(userRepository.deleteAllByEmailVerifiedFalseAndCreatedDateBefore(any(Instant.class))).thenReturn(1);

        scheduler.cleanupUnverifiedAccounts();

        verify(tokenRepository).deleteAllByUnverifiedUserCreatedDateBefore(any(Instant.class));
        verify(userRepository).deleteAllByEmailVerifiedFalseAndCreatedDateBefore(any(Instant.class));
    }

    @Test
    void cleanupUnverifiedAccountsWhenNoneFoundShouldStillCallRepositories() {
        when(tokenRepository.deleteAllByUnverifiedUserCreatedDateBefore(any(Instant.class))).thenReturn(0);
        when(userRepository.deleteAllByEmailVerifiedFalseAndCreatedDateBefore(any(Instant.class))).thenReturn(0);

        scheduler.cleanupUnverifiedAccounts();

        verify(tokenRepository).deleteAllByUnverifiedUserCreatedDateBefore(any(Instant.class));
        verify(userRepository).deleteAllByEmailVerifiedFalseAndCreatedDateBefore(any(Instant.class));
    }
}
