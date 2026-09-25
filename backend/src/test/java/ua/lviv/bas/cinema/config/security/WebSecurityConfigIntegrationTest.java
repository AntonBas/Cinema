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
    void requestWithInvalidJwtCookieShouldReturnUnauthorizedJsonNotOauthRedirect() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/bookings/1"))
                .header("Cookie", "jwt=garbage.token").GET().build();

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

    @Test
    void stateChangingRequestWithoutRequestedWithHeaderShouldBeRejected() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"email\":\"a@b.c\",\"password\":\"x\"}")).build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.headers().firstValue("Content-Type")).hasValueSatisfying(
                contentType -> assertThat(contentType).startsWith("application/json"));
        assertThat(response.body()).contains("\"statusCode\":403").contains("X-Requested-With");
    }

    @Test
    void stateChangingRequestWithRequestedWithHeaderShouldPassCsrfFilter() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/auth/login"))
                .header("Content-Type", "application/json").header("X-Requested-With", "XMLHttpRequest")
                .POST(HttpRequest.BodyPublishers.ofString("{\"email\":\"a@b.c\",\"password\":\"x\"}")).build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isNotEqualTo(403);
    }

    @Test
    void preflightFromUnknownOriginShouldNotAllowRequestedWithHeader() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/auth/login"))
                .header("Origin", "https://evil.example.com")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "x-requested-with,content-type")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.headers().firstValue("Access-Control-Allow-Origin")).isEmpty();
    }
}
