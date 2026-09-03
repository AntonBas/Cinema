package ua.lviv.bas.cinema.bonus.service;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import ua.lviv.bas.cinema.bonus.domain.BonusCard;
import ua.lviv.bas.cinema.bonus.domain.BonusTransaction;
import ua.lviv.bas.cinema.bonus.domain.BonusTransactionType;
import ua.lviv.bas.cinema.bonus.dto.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.bonus.repository.BonusCardRepository;
import ua.lviv.bas.cinema.bonus.repository.BonusTransactionRepository;
import ua.lviv.bas.cinema.config.NoOpCacheTestConfig;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("ci")
@Import({TestcontainersConfig.class, NoOpCacheTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BonusQueryServiceIntegrationTest {

    @Autowired
    private BonusQueryService bonusQueryService;
    @Autowired
    private BonusCardRepository bonusCardRepository;
    @Autowired
    private BonusTransactionRepository bonusTransactionRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void getTransactionsShouldReturnCorrectRunningBalancePerTransaction() {
        var user = userRepository.save(buildUser());
        var bonusCard = bonusCardRepository.save(BonusCard.builder().user(user).pointsBalance(140).build());

        var tx1 = bonusTransactionRepository
                .save(buildTransaction(bonusCard, BonusTransactionType.WELCOME_BONUS, 100));
        var tx2 = bonusTransactionRepository
                .save(buildTransaction(bonusCard, BonusTransactionType.PAYMENT_ACCRUAL, 50));
        var tx3 = bonusTransactionRepository
                .save(buildTransaction(bonusCard, BonusTransactionType.BOOKING_SPEND, -30));
        var tx4 = bonusTransactionRepository
                .save(buildTransaction(bonusCard, BonusTransactionType.PAYMENT_ACCRUAL, 20));

        var page = bonusQueryService.getTransactions(user.getId(), PageRequest.of(0, 10));
        Map<Long, Integer> newBalanceById = page.getContent().stream()
                .collect(Collectors.toMap(BonusTransactionResponse::id, BonusTransactionResponse::newBalance));

        assertThat(newBalanceById.get(tx1.getId())).isEqualTo(100);
        assertThat(newBalanceById.get(tx2.getId())).isEqualTo(150);
        assertThat(newBalanceById.get(tx3.getId())).isEqualTo(120);
        assertThat(newBalanceById.get(tx4.getId())).isEqualTo(140);
    }

    private User buildUser() {
        return User.builder().email("zztest.bonus@test.com").firstName("Test").lastName("User")
                .dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv").phoneNumber("+380000000016")
                .password("hashed-password").userRole(UserRole.ROLE_USER).enabled(true).build();
    }

    private BonusTransaction buildTransaction(BonusCard bonusCard, BonusTransactionType type, int pointsChange) {
        return BonusTransaction.builder().bonusCard(bonusCard).type(type).pointsChange(pointsChange).build();
    }
}
