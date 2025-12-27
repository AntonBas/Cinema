package ua.lviv.bas.cinema.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.repository.EmailTokenRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTokenCleanupScheduler {

	private final EmailTokenRepository tokenRepository;

	@Scheduled(cron = "0 */30 * * * *")
	@Transactional
	public void cleanupExpiredTokens() {
		LocalDateTime now = LocalDateTime.now();
		int deletedCount = tokenRepository.deleteByExpiresAtBefore(now);

		if (deletedCount > 0) {
			log.info("Cleaned up {} expired email tokens", deletedCount);
		}
	}

	@Scheduled(cron = "0 0 3 * * *")
	@Transactional
	public void cleanupOldConfirmedTokens() {
		LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
		int deletedCount = tokenRepository.deleteByConfirmedTrueAndConfirmedAtBefore(weekAgo);

		if (deletedCount > 0) {
			log.info("Cleaned up {} old confirmed email tokens", deletedCount);
		}
	}
}
