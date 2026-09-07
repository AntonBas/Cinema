package ua.lviv.bas.cinema.config.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ua.lviv.bas.cinema.config.http.RequestCorrelationFilter;

import java.util.UUID;

@Slf4j
@Configuration
@Profile("!ci")
@EnableScheduling
@EnableAsync
public class SchedulerConfig {

    @Bean
    public TaskScheduler taskScheduler() {
        var scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("scheduled-task-");
        scheduler.setErrorHandler(t -> log.error("Uncaught exception in scheduled task", t));
        scheduler.setTaskDecorator(runnable -> () -> {
            MDC.put(RequestCorrelationFilter.CORRELATION_ID_MDC_KEY, UUID.randomUUID().toString());
            try {
                runnable.run();
            } finally {
                MDC.remove(RequestCorrelationFilter.CORRELATION_ID_MDC_KEY);
            }
        });
        return scheduler;
    }
}