package ua.lviv.bas.cinema.config.security;

import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.user.User;
import ua.lviv.bas.cinema.user.UserRole;
import ua.lviv.bas.cinema.user.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("ci")
@Import(TestcontainersConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CustomUserDetailsServiceCacheIntegrationTest {

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
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void cachedTokenPrincipalShouldNotContainPasswordHash() {
        var saved = userRepository.save(buildUser("no.password@test.com", UserRole.ROLE_USER));

        customUserDetailsService.loadTokenPrincipal(saved.getEmail());

        var cachedJson = redisTemplate.opsForValue().get("v3::userDetails::" + saved.getEmail());
        assertThat(cachedJson).isNotNull().doesNotContain("\"password\"").doesNotContain("hashed-password");
        assertThat(customUserDetailsService.loadTokenPrincipal(saved.getEmail()).getPassword()).isNull();
    }

    @Test
    void loadUserByUsernameShouldBypassCacheAndKeepPasswordForLogin() {
        var saved = userRepository.save(buildUser("login@test.com", UserRole.ROLE_USER));
        customUserDetailsService.loadTokenPrincipal(saved.getEmail());

        assertThat(customUserDetailsService.loadUserByUsername(saved.getEmail()).getPassword())
                .isEqualTo("hashed-password");
    }

    @Test
    void loadTokenPrincipalShouldBeServedFromCacheAfterFirstLoad() {
        var saved = userRepository.save(buildUser("original@test.com", UserRole.ROLE_USER));

        var firstLoad = customUserDetailsService.loadTokenPrincipal(saved.getEmail());
        assertThat(firstLoad.getRole()).isEqualTo(UserRole.ROLE_USER.name());

        var staleEntity = userRepository.findById(saved.getId()).orElseThrow();
        staleEntity.setUserRole(UserRole.ROLE_ADMIN);
        userRepository.save(staleEntity);

        var cachedLoad = customUserDetailsService.loadTokenPrincipal(saved.getEmail());
        assertThat(cachedLoad.getRole()).isEqualTo(UserRole.ROLE_USER.name());
    }

    @Test
    void evictShouldForceFreshLoadFromDatabase() {
        var saved = userRepository.save(buildUser("evict@test.com", UserRole.ROLE_USER));

        customUserDetailsService.loadTokenPrincipal(saved.getEmail());

        var updated = userRepository.findById(saved.getId()).orElseThrow();
        updated.setUserRole(UserRole.ROLE_ADMIN);
        userRepository.save(updated);
        customUserDetailsService.evict(saved.getEmail());

        var freshLoad = customUserDetailsService.loadTokenPrincipal(saved.getEmail());
        assertThat(freshLoad.getRole()).isEqualTo(UserRole.ROLE_ADMIN.name());
    }

    @Test
    void evictInsideTransactionShouldNotLetConcurrentReaderCacheStaleValueBeforeCommit() throws Exception {
        var saved = userRepository.save(buildUser("tx.evict@test.com", UserRole.ROLE_USER));
        customUserDetailsService.loadTokenPrincipal(saved.getEmail());
        var transactionTemplate = new TransactionTemplate(transactionManager);
        var executor = Executors.newSingleThreadExecutor();

        transactionTemplate.executeWithoutResult(status -> {
            var user = userRepository.findById(saved.getId()).orElseThrow();
            user.setUserRole(UserRole.ROLE_ADMIN);
            userRepository.saveAndFlush(user);
            customUserDetailsService.evict(saved.getEmail());
            try {
                executor.submit(() -> customUserDetailsService.loadTokenPrincipal(saved.getEmail()))
                        .get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        executor.shutdown();

        var afterCommit = customUserDetailsService.loadTokenPrincipal(saved.getEmail());
        assertThat(afterCommit.getRole()).isEqualTo(UserRole.ROLE_ADMIN.name());
    }

    private User buildUser(String email, UserRole role) {
        return User.builder().email(email).firstName("Test").lastName("User")
                .dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv").phoneNumber("+380000000018")
                .password("hashed-password").userRole(role).enabled(true).emailVerified(true).build();
    }
}
