package ua.lviv.bas.cinema.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "management.server.port=")
@ActiveProfiles("ci")
@Import({TestcontainersConfig.class, NoOpCacheTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ActuatorAccessIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void healthShouldStayPublic() throws Exception {
        var status = mockMvc.perform(get("/actuator/health")).andReturn().getResponse().getStatus();

        assertThat(status).isNotIn(401, 403);
    }

    @Test
    void metricsShouldBeForbiddenForRegularUser() throws Exception {
        mockMvc.perform(get("/actuator/metrics").with(user("user@test.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void metricsShouldBeAvailableToAdmin() throws Exception {
        mockMvc.perform(get("/actuator/metrics").with(user("admin@test.com").roles("ADMIN")))
                .andExpect(status().isOk());
    }
}
