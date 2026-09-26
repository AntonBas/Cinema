package ua.lviv.bas.cinema.config.async;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ua.lviv.bas.cinema.config.http.RequestCorrelationFilter;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
public class AsyncConfig implements AsyncConfigurer {

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
        executor.setRejectedExecutionHandler(runInCallerWhenSaturated(EMAIL_EXECUTOR));
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
        executor.setRejectedExecutionHandler(runInCallerWhenSaturated(AUDIT_LOG_EXECUTOR));
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (exception, method, params) -> log.error("Async task {}.{} failed",
                method.getDeclaringClass().getSimpleName(), method.getName(), exception);
    }

    private RejectedExecutionHandler runInCallerWhenSaturated(String executorName) {
        var callerRuns = new ThreadPoolExecutor.CallerRunsPolicy();
        return (task, pool) -> {
            log.warn("{} queue is full, running the task in the calling thread", executorName);
            callerRuns.rejectedExecution(task, pool);
        };
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
