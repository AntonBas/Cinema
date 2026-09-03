package ua.lviv.bas.cinema.user.service;

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
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
@ActiveProfiles("ci")
@Import(TestcontainersConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserServiceCacheIntegrationTest {

    private static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        redis.start();
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Test
    void getUserResponseShouldBeServedFromRedisWithoutLazyInitializationException() {
        var saved = userRepository.save(buildUser("Original"));

        var firstResponse = userService.getUserResponse(saved.getId());
        assertThat(firstResponse.firstName()).isEqualTo("Original");

        var staleEntity = userRepository.findById(saved.getId()).orElseThrow();
        staleEntity.setFirstName("ChangedDirectlyInDb");
        userRepository.save(staleEntity);

        assertThatCode(() -> userService.getUserResponse(saved.getId())).doesNotThrowAnyException();

        var cachedResponse = userService.getUserResponse(saved.getId());
        assertThat(cachedResponse.firstName()).isEqualTo("Original");
    }

    private User buildUser(String firstName) {
        return User.builder().email("zztest.cache.user@test.com").firstName(firstName).lastName("User")
                .dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv").phoneNumber("+380000000017")
                .password("hashed-password").userRole(UserRole.ROLE_USER).enabled(true).build();
    }
}
