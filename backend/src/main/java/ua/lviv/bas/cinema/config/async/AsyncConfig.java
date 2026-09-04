package ua.lviv.bas.cinema.config.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {

    public static final String EMAIL_EXECUTOR = "emailTaskExecutor";
    public static final String AUDIT_LOG_EXECUTOR = "auditLogTaskExecutor";

    @Bean(EMAIL_EXECUTOR)
    public ThreadPoolTaskExecutor emailTaskExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("email-async-");
        executor.initialize();
        return executor;
    }

    @Bean(AUDIT_LOG_EXECUTOR)
    public ThreadPoolTaskExecutor auditLogTaskExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("audit-log-async-");
        executor.initialize();
        return executor;
    }
}
