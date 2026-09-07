package ua.lviv.bas.cinema.config.async;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import ua.lviv.bas.cinema.config.http.RequestCorrelationFilter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncConfigTest {

    private final AsyncConfig asyncConfig = new AsyncConfig();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void propagatesCorrelationIdFromCallerThreadIntoAsyncThread() throws InterruptedException {
        MDC.put(RequestCorrelationFilter.CORRELATION_ID_MDC_KEY, "test-correlation-id");
        var executor = asyncConfig.emailTaskExecutor();
        var capturedId = new AtomicReference<String>();
        var latch = new CountDownLatch(1);

        executor.execute(() -> {
            capturedId.set(MDC.get(RequestCorrelationFilter.CORRELATION_ID_MDC_KEY));
            latch.countDown();
        });

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(capturedId.get()).isEqualTo("test-correlation-id");
        executor.shutdown();
    }

    @Test
    void doesNotLeakCorrelationIdIntoTaskSubmittedWithoutOne() throws InterruptedException {
        MDC.put(RequestCorrelationFilter.CORRELATION_ID_MDC_KEY, "test-correlation-id");
        var executor = asyncConfig.auditLogTaskExecutor();
        var firstLatch = new CountDownLatch(1);
        executor.execute(firstLatch::countDown);
        assertThat(firstLatch.await(5, TimeUnit.SECONDS)).isTrue();

        MDC.clear();
        var secondLatch = new CountDownLatch(1);
        var leakedId = new AtomicReference<String>();
        executor.execute(() -> {
            leakedId.set(MDC.get(RequestCorrelationFilter.CORRELATION_ID_MDC_KEY));
            secondLatch.countDown();
        });

        assertThat(secondLatch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(leakedId.get()).isNull();
        executor.shutdown();
    }
}
