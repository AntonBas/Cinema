package ua.lviv.bas.cinema.bonus.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ua.lviv.bas.cinema.bonus.domain.BonusCard;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.bonus.repository.BonusCardRepository;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import java.time.LocalDate;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("ci")
@Import(TestcontainersConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BonusCardConcurrencyTest {

    private static final int POINTS_A = 30;
    private static final int POINTS_B = 45;

    @Autowired
    private BonusLedgerService bonusLedgerService;
    @Autowired
    private BonusCardRepository bonusCardRepository;
    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(buildUser("bonus.concurrency@test.com"));
        bonusCardRepository.save(BonusCard.builder().user(user).pointsBalance(0).welcomeBonusReceived(true).build());
    }

    @Test
    void concurrentPromotionPointAdditionsShouldNotLoseUpdates() throws Exception {
        var readyLatch = new CountDownLatch(2);
        var startLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Exception> addPointsA = () -> attemptAddPoints(readyLatch, startLatch, POINTS_A, "Promo A");
        Callable<Exception> addPointsB = () -> attemptAddPoints(readyLatch, startLatch, POINTS_B, "Promo B");

        Future<Exception> resultA = executor.submit(addPointsA);
        Future<Exception> resultB = executor.submit(addPointsB);

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();

        Exception outcomeA = resultA.get(10, TimeUnit.SECONDS);
        Exception outcomeB = resultB.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(outcomeA).isNull();
        assertThat(outcomeB).isNull();

        var card = bonusCardRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(card.getPointsBalance()).isEqualTo(POINTS_A + POINTS_B);
    }

    private Exception attemptAddPoints(CountDownLatch readyLatch, CountDownLatch startLatch, int points,
                                       String promotionTitle) {
        try {
            readyLatch.countDown();
            startLatch.await();
            bonusLedgerService.addPromotionPoints(user, points, promotionTitle);
            return null;
        } catch (Exception e) {
            return e;
        }
    }

    private User buildUser(String email) {
        return User.builder().email(email).firstName("Test").lastName("User")
                .dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv").phoneNumber("+380000000011")
                .password("hashed-password").userRole(UserRole.ROLE_USER).enabled(true).build();
    }
}
