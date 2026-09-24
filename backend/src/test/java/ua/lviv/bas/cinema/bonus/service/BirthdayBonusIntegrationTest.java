package ua.lviv.bas.cinema.bonus.service;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import ua.lviv.bas.cinema.bonus.domain.BonusCard;
import ua.lviv.bas.cinema.bonus.domain.BonusTransaction;
import ua.lviv.bas.cinema.bonus.domain.BonusTransactionType;
import ua.lviv.bas.cinema.bonus.repository.BonusCardRepository;
import ua.lviv.bas.cinema.bonus.repository.BonusTransactionRepository;
import ua.lviv.bas.cinema.common.CinemaTime;
import ua.lviv.bas.cinema.config.NoOpCacheTestConfig;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.domain.VerificationStatus;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("ci")
@Import({TestcontainersConfig.class, NoOpCacheTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BirthdayBonusIntegrationTest {

    @Autowired
    private BonusLedgerService bonusLedgerService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BonusCardRepository bonusCardRepository;
    @Autowired
    private BonusTransactionRepository bonusTransactionRepository;

    @Test
    void birthdayBonusShouldBeAwardedAgainInAYearAfterAPreviousBirthdayBonus() {
        var today = CinemaTime.today();
        var user = userRepository.save(User.builder().email("zztest.birthday." + System.nanoTime() + "@test.com")
                .firstName("Test").lastName("User").dateOfBirth(today.minusYears(30)).city("Lviv")
                .phoneNumber("+38" + String.format("%010d", System.nanoTime() % 10_000_000_000L))
                .password("hashed-password").userRole(UserRole.ROLE_USER).enabled(true).emailVerified(true)
                .verificationStatus(VerificationStatus.VERIFIED).verifiedAt(Instant.now()).build());
        var card = bonusCardRepository.save(BonusCard.builder().user(user).pointsBalance(100)
                .welcomeBonusReceived(true).lastBirthdayBonusDate(today.minusYears(1)).build());
        bonusTransactionRepository.save(BonusTransaction.builder().bonusCard(card)
                .type(BonusTransactionType.BIRTHDAY_BONUS).pointsChange(100).balanceAfter(100)
                .referenceId("USER_" + user.getId()).build());

        bonusLedgerService.awardBirthdayBonus(user);

        var updated = bonusCardRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(updated.getLastBirthdayBonusDate()).isEqualTo(today);
        assertThat(updated.getPointsBalance()).isGreaterThan(100);
    }
}
