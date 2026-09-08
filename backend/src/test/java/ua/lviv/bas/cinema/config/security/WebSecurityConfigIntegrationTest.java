package ua.lviv.bas.cinema.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ua.lviv.bas.cinema.config.TestcontainersConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("ci")
@Import(TestcontainersConfig.class)
class WebSecurityConfigIntegrationTest {

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

    @Test
    void unauthenticatedRequestToProtectedEndpointShouldReturnUnauthorizedJsonNotOauthRedirect() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/bookings/1")).GET()
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.headers().firstValue("Location")).isEmpty();
        assertThat(response.body()).contains("\"message\":\"Not authenticated\"");
    }

    @Test
    void requestWithInvalidBearerTokenShouldReturnUnauthorizedJsonNotOauthRedirect() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/bookings/1"))
                .header("Authorization", "Bearer garbage.token").GET().build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.headers().firstValue("Location")).isEmpty();
    }

    @Test
    void permitAllEndpointShouldNotRedirectWhenUnauthenticated() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/movies")).GET().build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isNotEqualTo(302);
        assertThat(response.headers().firstValue("Location")).isEmpty();
    }
}
