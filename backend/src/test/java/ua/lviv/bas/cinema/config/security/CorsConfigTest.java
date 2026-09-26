package ua.lviv.bas.cinema.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorsConfigTest {

    @Test
    void shouldAllowOnlyExactlyConfiguredOrigins() {
        var configuration = buildConfiguration("https://cinema-bas.vercel.app, https://admin.example.com");

        assertThat(configuration.checkOrigin("https://cinema-bas.vercel.app"))
                .isEqualTo("https://cinema-bas.vercel.app");
        assertThat(configuration.checkOrigin("https://admin.example.com")).isEqualTo("https://admin.example.com");
        assertThat(configuration.checkOrigin("https://cinema-bas-evil.vercel.app")).isNull();
        assertThat(configuration.getAllowCredentials()).isTrue();
    }

    @Test
    void shouldRejectWildcardOriginsAtStartup() {
        assertThatThrownBy(() -> buildConfiguration("https://cinema-bas*.vercel.app"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("https://cinema-bas*.vercel.app");
    }

    @Test
    void shouldRejectBlankOrigins() {
        assertThatThrownBy(() -> buildConfiguration(" , "))
                .isInstanceOf(IllegalStateException.class);
    }

    private CorsConfiguration buildConfiguration(String allowedOrigins) {
        var corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", allowedOrigins);
        var request = new MockHttpServletRequest("GET", "/api/movies");
        return corsConfig.corsConfigurationSource().getCorsConfiguration(request);
    }
}
