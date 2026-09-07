package ua.lviv.bas.cinema.config.async;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ua.lviv.bas.cinema.config.http.RequestCorrelationFilter;

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
        executor.setTaskDecorator(mdcPropagatingTaskDecorator());
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
        executor.setTaskDecorator(mdcPropagatingTaskDecorator());
        executor.initialize();
        return executor;
    }

    private TaskDecorator mdcPropagatingTaskDecorator() {
        return runnable -> {
            var callerContext = MDC.getCopyOfContextMap();
            return () -> {
                if (callerContext != null) {
                    MDC.setContextMap(callerContext);
                }
                try {
                    runnable.run();
                } finally {
                    MDC.remove(RequestCorrelationFilter.CORRELATION_ID_MDC_KEY);
                }
            };
        };
    }
}
