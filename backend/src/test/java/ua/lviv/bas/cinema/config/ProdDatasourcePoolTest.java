package ua.lviv.bas.cinema.config;

import java.io.IOException;
import java.time.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class ProdDatasourcePoolTest {

    private static final Duration NEON_SCALE_TO_ZERO_DELAY = Duration.ofMinutes(5);

    private static final StandardEnvironment prodEnvironment = new StandardEnvironment();

    @BeforeAll
    static void loadProdProfile() throws IOException {
        new YamlPropertySourceLoader().load("application-prod", new ClassPathResource("application-prod.yml"))
                .forEach(prodEnvironment.getPropertySources()::addLast);
    }

    @Test
    void poolShouldNotKeepIdleConnectionsOpen() {
        assertThat(prodEnvironment.getProperty("spring.datasource.hikari.minimum-idle", Integer.class))
                .isZero();
    }

    @Test
    void poolShouldNotPingIdleConnections() {
        assertThat(prodEnvironment.getProperty("spring.datasource.hikari.keepalive-time", Long.class))
                .isZero();
    }

    @Test
    void idleConnectionsShouldCloseBeforeNeonCouldScaleToZero() {
        var idleTimeout = Duration.ofMillis(
                prodEnvironment.getProperty("spring.datasource.hikari.idle-timeout", Long.class));

        assertThat(idleTimeout).isLessThan(NEON_SCALE_TO_ZERO_DELAY);
    }
}
