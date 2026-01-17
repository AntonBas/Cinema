package ua.lviv.bas.cinema.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.repository.EmailTokenRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTokenCleanupScheduler {

	private final EmailTokenRepository tokenRepository;

	@Scheduled(cron = "${scheduler.email-token.cleanup-expired-cron:0 */30 * * * *}")
	@Transactional
	public void cleanupExpiredTokens() {
		log.debug("Starting expired email tokens cleanup");

		LocalDateTime now = LocalDateTime.now();
		int deletedCount = tokenRepository.deleteByExpiresAtBefore(now);

		if (deletedCount > 0) {
			log.info("Cleaned up {} expired email tokens", deletedCount);
		} else {
			log.debug("No expired email tokens to clean up");
		}
	}

	@Scheduled(cron = "${scheduler.email-token.cleanup-confirmed-cron:0 0 3 * * *}")
	@Transactional
	public void cleanupOldConfirmedTokens() {
		log.debug("Starting old confirmed email tokens cleanup");

		LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
		int deletedCount = tokenRepository.deleteByConfirmedTrueAndConfirmedAtBefore(weekAgo);

		if (deletedCount > 0) {
			log.info("Cleaned up {} old confirmed email tokens", deletedCount);
		} else {
			log.debug("No old confirmed email tokens to clean up");
		}
	}
}