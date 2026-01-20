package ua.lviv.bas.cinema.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.repository.UserRepository;
import ua.lviv.bas.cinema.service.user.BonusService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BirthdayBonusScheduler {

	private final UserRepository userRepository;
	private final BonusService bonusService;

	@Scheduled(cron = "${scheduler.birthday-bonus.cron:0 0 9 * * *}")
	@Transactional
	public void awardBirthdayBonuses() {
		log.info("Starting birthday bonus distribution");

		LocalDate today = LocalDate.now();
		int dayOfMonth = today.getDayOfMonth();
		int month = today.getMonthValue();

		List<User> birthdayUsers = userRepository.findVerifiedUsersWithBirthday(VerificationStatus.VERIFIED, dayOfMonth,
				month);

		if (birthdayUsers.isEmpty()) {
			log.info("No verified users with birthday today");
			return;
		}

		log.info("Found {} users with birthday today", birthdayUsers.size());

		int awardedCount = 0;
		int failedCount = 0;

		for (User user : birthdayUsers) {
			try {
				bonusService.awardBirthdayBonus(user);
				awardedCount++;
				log.info("Awarded birthday bonus to user {} ({})", user.getId(), user.getEmail());
			} catch (Exception e) {
				failedCount++;
				log.error("Failed to award birthday bonus to user {}: {}", user.getId(), e.getMessage());
			}
		}

		log.info("Birthday bonus distribution completed. Awarded: {}, Failed: {}", awardedCount, failedCount);
	}
}