package ua.lviv.bas.cinema.user.scheduler;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.user.repository.EmailTokenRepository;
import ua.lviv.bas.cinema.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnverifiedAccountCleanupScheduler {

    private final UserRepository userRepository;
    private final EmailTokenRepository tokenRepository;

    @Value("${scheduler.unverified-account.grace-period-hours:48}")
    private int gracePeriodHours;

    @Scheduled(cron = "${scheduler.unverified-account.cleanup-cron:0 30 3 * * *}")
    @Transactional
    public void cleanupUnverifiedAccounts() {
        log.debug("Starting unverified account cleanup");

        LocalDateTime cutoff = LocalDateTime.now().minusHours(gracePeriodHours);
        int deletedTokens = tokenRepository.deleteAllByUnverifiedUserCreatedDateBefore(cutoff);
        int deletedAccounts = userRepository.deleteAllByEnabledFalseAndCreatedDateBefore(cutoff);

        if (deletedAccounts > 0) {
            log.info("Cleaned up {} unverified accounts ({} tokens) older than {}h", deletedAccounts, deletedTokens,
                    gracePeriodHours);
        } else {
            log.debug("No unverified accounts to clean up");
        }
    }
}
