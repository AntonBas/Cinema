package ua.lviv.bas.cinema.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.repository.UserRepository;
import ua.lviv.bas.cinema.service.user.UserBonusService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BirthdayBonusScheduler {

	private final UserRepository userRepository;
	private final UserBonusService bonusUserService;

	@Scheduled(cron = "${scheduler.birthday-bonus.cron:0 0 9 * * *}")
	@Async("taskExecutor")
	@Transactional
	public void awardBirthdayBonuses() {
		log.info("Starting automatic birthday bonus distribution");

		LocalDate today = LocalDate.now();
		int dayOfMonth = today.getDayOfMonth();
		int month = today.getMonthValue();

		List<User> birthdayUsers = userRepository.findVerifiedUsersWithBirthday(VerificationStatus.VERIFIED, dayOfMonth,
				month);

		if (birthdayUsers.isEmpty()) {
			log.info("No verified users with birthday today");
			return;
		}

		log.info("Found {} verified users with birthday today", birthdayUsers.size());

		int awardedCount = 0;
		int failedCount = 0;

		for (User user : birthdayUsers) {
			try {
				bonusUserService.awardBirthdayBonus(user);
				awardedCount++;
				log.debug("Processed birthday bonus for user {}", user.getId());
			} catch (Exception e) {
				log.error("Failed to process birthday bonus for user {}: {}", user.getId(), e.getMessage(), e);
				failedCount++;
			}
		}

		log.info("Birthday bonus distribution completed. Processed: {}, Failed: {}", awardedCount, failedCount);
	}
}