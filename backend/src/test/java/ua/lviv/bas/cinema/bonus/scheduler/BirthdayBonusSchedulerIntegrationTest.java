package ua.lviv.bas.cinema.bonus.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ua.lviv.bas.cinema.bonus.domain.BonusCard;
import ua.lviv.bas.cinema.bonus.repository.BonusCardRepository;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.domain.VerificationStatus;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("ci")
@Import(TestcontainersConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BirthdayBonusSchedulerIntegrationTest {

    @Autowired
    private BirthdayBonusScheduler birthdayBonusScheduler;
    @Autowired
    private UserRepository userRepository;

    @MockitoSpyBean
    private BonusCardRepository bonusCardRepository;

    @Test
    void awardBirthdayBonusesWhenOneUserFailsShouldStillPersistTheOthers() {
        LocalDate today = LocalDate.now();
        LocalDate dateOfBirth = LocalDate.of(1990, today.getMonthValue(), today.getDayOfMonth());

        var failingUser = userRepository.save(buildUser("failing.birthday@test.com", dateOfBirth));
        var goodUser = userRepository.save(buildUser("good.birthday@test.com", dateOfBirth));

        doThrow(new RuntimeException("Simulated failure while awarding birthday bonus")).when(bonusCardRepository)
                .save(argThat((BonusCard card) -> card.getUser().getId().equals(failingUser.getId())));

        birthdayBonusScheduler.awardBirthdayBonuses();

        var goodUserCard = bonusCardRepository.findByUserId(goodUser.getId());
        assertThat(goodUserCard).isPresent();
        assertThat(goodUserCard.get().getLastBirthdayBonusDate()).isEqualTo(today);
        assertThat(goodUserCard.get().getPointsBalance()).isGreaterThan(0);
    }

    private User buildUser(String email, LocalDate dateOfBirth) {
        return User.builder().email(email).firstName("Test").lastName("User").dateOfBirth(dateOfBirth).city("Lviv")
                .phoneNumber("+380000000030").password("hashed-password").userRole(UserRole.ROLE_USER)
                .verificationStatus(VerificationStatus.VERIFIED).enabled(true).build();
    }
}
