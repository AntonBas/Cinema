package ua.lviv.bas.cinema.config.security;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.repository.UserRepository;

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
    }

    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private UserRepository userRepository;

    @Test
    void loadUserByUsernameShouldBeServedFromCacheAfterFirstLoad() {
        var saved = userRepository.save(buildUser("original@test.com", UserRole.ROLE_USER));

        var firstLoad = (CustomUserDetails) customUserDetailsService.loadUserByUsername(saved.getEmail());
        assertThat(firstLoad.getRole()).isEqualTo(UserRole.ROLE_USER.name());

        var staleEntity = userRepository.findById(saved.getId()).orElseThrow();
        staleEntity.setUserRole(UserRole.ROLE_ADMIN);
        userRepository.save(staleEntity);

        var cachedLoad = (CustomUserDetails) customUserDetailsService.loadUserByUsername(saved.getEmail());
        assertThat(cachedLoad.getRole()).isEqualTo(UserRole.ROLE_USER.name());
    }

    @Test
    void evictShouldForceFreshLoadFromDatabase() {
        var saved = userRepository.save(buildUser("evict@test.com", UserRole.ROLE_USER));

        customUserDetailsService.loadUserByUsername(saved.getEmail());

        var updated = userRepository.findById(saved.getId()).orElseThrow();
        updated.setUserRole(UserRole.ROLE_ADMIN);
        userRepository.save(updated);
        customUserDetailsService.evict(saved.getEmail());

        var freshLoad = (CustomUserDetails) customUserDetailsService.loadUserByUsername(saved.getEmail());
        assertThat(freshLoad.getRole()).isEqualTo(UserRole.ROLE_ADMIN.name());
    }

    private User buildUser(String email, UserRole role) {
        return User.builder().email(email).firstName("Test").lastName("User")
                .dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv").phoneNumber("+380000000018")
                .password("hashed-password").userRole(role).enabled(true).build();
    }
}
