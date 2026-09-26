package ua.lviv.bas.cinema.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import ua.lviv.bas.cinema.config.NoOpCacheTestConfig;
import ua.lviv.bas.cinema.config.TestcontainersConfig;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("ci")
@Import({TestcontainersConfig.class, NoOpCacheTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AdminEndpointAccessIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @ParameterizedTest
    @CsvSource({
            "ADMIN, /api/admin/bookings, 200",
            "CASHIER, /api/admin/bookings, 200",
            "CONTENT_MANAGER, /api/admin/bookings, 403",
            "USER, /api/admin/bookings, 403",
            "ADMIN, /api/admin/refunds, 200",
            "CASHIER, /api/admin/refunds, 200",
            "CONTENT_MANAGER, /api/admin/refunds, 403",
            "CASHIER, /api/admin/bonus/users/1/transactions, 200",
            "CONTENT_MANAGER, /api/admin/bonus/users/1/transactions, 403",
            "CASHIER, /api/admin/bonus/rules, 403",
            "ADMIN, /api/admin/bonus/rules, 200",
            "CASHIER, /api/admin/users, 200"
    })
    void adminEndpointsShouldEnforceRoles(String role, String path, int expectedStatus) throws Exception {
        mockMvc.perform(get(path).with(user("access-test@test.com").roles(role)))
                .andExpect(status().is(expectedStatus));
    }
}
