package ua.lviv.bas.cinema.user.service;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import ua.lviv.bas.cinema.config.NoOpCacheTestConfig;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.exception.domain.auth.EmailAlreadyExistsException;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.dto.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("ci")
@Import({TestcontainersConfig.class, NoOpCacheTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class EmailCaseInsensitivityIntegrationTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Test
    void registrationShouldStoreLowercaseEmailAndRejectCaseVariantDuplicates() {
        userService.register(request("ZZTest.Case@Example.com", "+380991112201"));

        var stored = userRepository.findByEmail("zztest.case@example.com").orElseThrow();
        assertThat(stored.getEmail()).isEqualTo("zztest.case@example.com");
        assertThat(userRepository.findByEmail("ZZTEST.CASE@EXAMPLE.COM")).isPresent();
        assertThat(userService.emailExists("Zztest.Case@example.com")).isTrue();

        assertThatThrownBy(() -> userService.register(request("zztest.CASE@example.com", "+380991112202")))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void databaseShouldRejectCaseVariantDuplicatesWrittenDirectly() {
        userRepository.saveAndFlush(user("zztest.direct@example.com", "+380991112203"));

        assertThatThrownBy(() -> userRepository.saveAndFlush(user("ZZTEST.Direct@example.com", "+380991112204")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private UserRegistrationRequest request(String email, String phone) {
        return new UserRegistrationRequest(email, "Test", "User", LocalDate.of(1995, 1, 1), "Lviv", phone,
                "Password1!", "Password1!");
    }

    private User user(String email, String phone) {
        return User.builder().email(email).firstName("Test").lastName("User").dateOfBirth(LocalDate.of(1995, 1, 1))
                .city("Lviv").phoneNumber(phone).password("hashed-password").userRole(UserRole.ROLE_USER)
                .enabled(true).emailVerified(true).build();
    }
}
