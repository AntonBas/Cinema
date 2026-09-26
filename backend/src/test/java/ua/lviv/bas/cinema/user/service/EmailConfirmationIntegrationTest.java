package ua.lviv.bas.cinema.user.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import ua.lviv.bas.cinema.bonus.repository.BonusCardRepository;
import ua.lviv.bas.cinema.config.NoOpCacheTestConfig;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.user.domain.EmailToken;
import ua.lviv.bas.cinema.user.domain.TokenType;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.repository.EmailTokenRepository;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

@SpringBootTest
@ActiveProfiles("ci")
@Import({TestcontainersConfig.class, NoOpCacheTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class EmailConfirmationIntegrationTest {

    @Autowired
    private EmailTokenService emailTokenService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailTokenRepository emailTokenRepository;
    @Autowired
    private BonusCardRepository bonusCardRepository;

    @Test
    void confirmEmailShouldCompleteAndAwardWelcomeBonusForUserWithoutBonusCard() {
        var user = userRepository.save(User.builder().email("zztest.confirm." + System.nanoTime() + "@test.com")
                .firstName("Test").lastName("User").dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv")
                .phoneNumber("+38" + String.format("%010d", System.nanoTime() % 10_000_000_000L))
                .password("hashed-password").userRole(UserRole.ROLE_USER).enabled(true).emailVerified(false)
                .build());
        var token = UUID.randomUUID().toString();
        emailTokenRepository.save(EmailToken.builder().token(token).user(user).type(TokenType.VERIFICATION)
                .createdAt(Instant.now()).expiresAt(Instant.now().plus(Duration.ofHours(1))).build());

        assertTimeoutPreemptively(Duration.ofSeconds(20), () -> emailTokenService.confirmEmail(token));

        assertThat(userRepository.findById(user.getId()).orElseThrow().isEmailVerified()).isTrue();
        var card = bonusCardRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(card.isWelcomeBonusReceived()).isTrue();
        assertThat(card.getPointsBalance()).isPositive();
    }
}
