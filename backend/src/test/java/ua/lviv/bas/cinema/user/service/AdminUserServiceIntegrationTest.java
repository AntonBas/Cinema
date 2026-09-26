package ua.lviv.bas.cinema.user.service;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import ua.lviv.bas.cinema.config.NoOpCacheTestConfig;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.exception.core.InvalidSortPropertyException;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.dto.response.AdminUserListResponse;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("ci")
@Import({TestcontainersConfig.class, NoOpCacheTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AdminUserServiceIntegrationTest {

    private static final String SEARCH = "zzsort";

    @Autowired
    private AdminUserService adminUserService;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        if (userRepository.findByEmail("zzsort.b@test.com").isPresent()) {
            return;
        }
        userRepository.save(buildUser("zzsort.b@test.com", "Alpha", "+380000000101"));
        userRepository.save(buildUser("zzsort.c@test.com", "Charlie", "+380000000102"));
        userRepository.save(buildUser("zzsort.a@test.com", "Bravo", "+380000000103"));
    }

    @Test
    void getUsersShouldSortByEmailAscending() {
        var page = adminUserService.getUsers(SEARCH, null, null, null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "email")));

        assertThat(page.getContent()).extracting(AdminUserListResponse::email)
                .containsExactly("zzsort.a@test.com", "zzsort.b@test.com", "zzsort.c@test.com");
    }

    @Test
    void getUsersShouldSortByLastNameDescending() {
        var page = adminUserService.getUsers(SEARCH, null, null, null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lastName")));

        assertThat(page.getContent()).extracting(AdminUserListResponse::email)
                .containsExactly("zzsort.c@test.com", "zzsort.a@test.com", "zzsort.b@test.com");
    }

    @Test
    void getUsersShouldDefaultToNewestFirst() {
        var page = adminUserService.getUsers(SEARCH, null, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(AdminUserListResponse::email)
                .containsExactly("zzsort.a@test.com", "zzsort.c@test.com", "zzsort.b@test.com");
    }

    @Test
    void getUsersShouldSortByTicketsCount() {
        var page = adminUserService.getUsers(SEARCH, null, null, null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "ticketsCount")));

        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void getUsersShouldRejectUnknownSortProperty() {
        assertThatThrownBy(() -> adminUserService.getUsers(SEARCH, null, null, null,
                PageRequest.of(0, 10, Sort.by("password"))))
                .isInstanceOf(InvalidSortPropertyException.class);
    }

    private User buildUser(String email, String lastName, String phone) {
        return User.builder().email(email).firstName("Test").lastName(lastName)
                .dateOfBirth(LocalDate.of(1995, 1, 1)).city("Lviv").phoneNumber(phone)
                .password("hashed-password").userRole(UserRole.ROLE_USER).enabled(true).emailVerified(true).build();
    }
}
