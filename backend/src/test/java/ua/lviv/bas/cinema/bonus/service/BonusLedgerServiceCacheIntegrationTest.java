package ua.lviv.bas.cinema.bonus.service;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import ua.lviv.bas.cinema.bonus.domain.BonusCard;
import ua.lviv.bas.cinema.bonus.domain.BonusTransaction;
import ua.lviv.bas.cinema.bonus.domain.BonusTransactionType;
import ua.lviv.bas.cinema.bonus.repository.BonusCardRepository;
import ua.lviv.bas.cinema.bonus.repository.BonusTransactionRepository;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("ci")
@Import(TestcontainersConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BonusLedgerServiceCacheIntegrationTest {

    private static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        redis.start();
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.cache.type", () -> "redis");
    }

    @Autowired
    private BonusQueryService bonusQueryService;
    @Autowired
    private BonusLedgerService bonusLedgerService;
    @Autowired
    private BonusCardRepository bonusCardRepository;
    @Autowired
    private BonusTransactionRepository bonusTransactionRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void mutatingOneUsersPointsShouldNotEvictAnotherUsersCachedBalance() {
        var userA = userRepository.save(buildUser("zztest.bonus.cache.a@test.com"));
        var userB = userRepository.save(buildUser("zztest.bonus.cache.b@test.com"));
        bonusCardRepository.save(BonusCard.builder().user(userA).pointsBalance(50).build());
        bonusCardRepository.save(BonusCard.builder().user(userB).pointsBalance(50).build());

        assertThat(bonusQueryService.getBalance(userA.getId()).pointsBalance()).isEqualTo(50);
        assertThat(bonusQueryService.getBalance(userB.getId()).pointsBalance()).isEqualTo(50);

        var cardB = bonusCardRepository.findByUserId(userB.getId()).orElseThrow();
        cardB.setPointsBalance(999);
        bonusCardRepository.save(cardB);

        bonusLedgerService.addPromotionPoints(userA, 20, "TEST_PROMO");

        assertThat(bonusQueryService.getBalance(userA.getId()).pointsBalance()).isEqualTo(70);
        assertThat(bonusQueryService.getBalance(userB.getId()).pointsBalance())
                .as("user B's cached balance must survive user A's points mutation")
                .isEqualTo(50);
    }

    @Test
    void mutatingOneUsersPointsShouldNotEvictAnotherUsersCachedTransactionPage() {
        var userA = userRepository.save(buildUser("zztest.bonus.tx.cache.a@test.com"));
        var userB = userRepository.save(buildUser("zztest.bonus.tx.cache.b@test.com"));
        var cardA = bonusCardRepository.save(BonusCard.builder().user(userA).pointsBalance(0).build());
        var cardB = bonusCardRepository.save(BonusCard.builder().user(userB).pointsBalance(0).build());
        var pageable = PageRequest.of(0, 10);

        assertThat(bonusQueryService.getTransactions(userA.getId(), pageable)).isEmpty();
        assertThat(bonusQueryService.getTransactions(userB.getId(), pageable)).isEmpty();

        bonusTransactionRepository.save(BonusTransaction.builder().bonusCard(cardB)
                .type(BonusTransactionType.PROMOTION_BONUS).pointsChange(30).balanceAfter(30)
                .referenceId("PROMOTION_direct-db-write").build());

        bonusLedgerService.addPromotionPoints(userA, 20, "TEST_PROMO_TX");

        assertThat(bonusQueryService.getTransactions(userA.getId(), pageable))
                .as("user A's transaction cache must be refreshed after A's own mutation")
                .hasSize(1);
        assertThat(bonusQueryService.getTransactions(userB.getId(), pageable))
                .as("user B's cached transaction page must survive user A's mutation")
                .isEmpty();
    }

    private User buildUser(String email) {
        return User.builder().email(email).firstName("Test").lastName("User").dateOfBirth(LocalDate.of(1995, 1, 1))
                .city("Lviv").phoneNumber("+380000000018").password("hashed-password").userRole(UserRole.ROLE_USER)
                .enabled(true).build();
    }
}
