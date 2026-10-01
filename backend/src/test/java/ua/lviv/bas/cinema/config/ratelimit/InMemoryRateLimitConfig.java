package ua.lviv.bas.cinema.config.ratelimit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("ci")
public class InMemoryRateLimitConfig {

    @Bean
    public InMemoryProxyManager rateLimitProxyManager() {
        return new InMemoryProxyManager();
    }
}
