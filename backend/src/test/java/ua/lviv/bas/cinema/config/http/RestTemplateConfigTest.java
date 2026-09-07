package ua.lviv.bas.cinema.config.http;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestTemplateConfigTest {

    private final RestTemplate restTemplate = new RestTemplateConfig().restTemplate();

    @Test
    void restTemplateShouldEnforceReadTimeoutUnderRealNetworkLag() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            int port = serverSocket.getLocalPort();
            Thread serverThread = new Thread(() -> {
                try (Socket socket = serverSocket.accept()) {
                    Thread.sleep(15_000);
                } catch (Exception ignored) {
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();

            long start = System.currentTimeMillis();

            assertThatThrownBy(() -> restTemplate.getForEntity("http://localhost:" + port + "/", String.class))
                    .isInstanceOf(ResourceAccessException.class)
                    .hasCauseInstanceOf(SocketTimeoutException.class);

            long elapsed = System.currentTimeMillis() - start;
            assertThat(elapsed).isGreaterThanOrEqualTo(9_500L).isLessThan(14_000L);
        }
    }
}
