package ua.lviv.bas.cinema.config.scheduling;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;

class ProdSchedulerIntervalsTest {

    private static final Duration MIN_PROD_INTERVAL = Duration.ofMinutes(10);
    private static final String BASE_PACKAGE = "ua.lviv.bas.cinema";

    private static final StandardEnvironment prodEnvironment = new StandardEnvironment();
    private static final List<Method> scheduledMethods = new ArrayList<>();

    @BeforeAll
    static void loadProdProfileAndScheduledMethods() throws IOException {
        new YamlPropertySourceLoader().load("application-prod", new ClassPathResource("application-prod.yml"))
                .forEach(prodEnvironment.getPropertySources()::addLast);

        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Component.class));
        for (var candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
            var type = ClassUtils.resolveClassName(candidate.getBeanClassName(), null);
            Arrays.stream(type.getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(Scheduled.class))
                    .forEach(scheduledMethods::add);
        }
    }

    @Test
    void shouldDiscoverScheduledJobs() {
        assertThat(scheduledMethods).hasSizeGreaterThanOrEqualTo(15);
    }

    @Test
    void fixedRateJobsShouldNotRunMoreOftenThanMinIntervalInProd() {
        scheduledMethods.forEach(method -> {
            var scheduled = method.getAnnotation(Scheduled.class);
            var expression = StringUtils.hasText(scheduled.fixedRateString()) ? scheduled.fixedRateString()
                    : scheduled.fixedDelayString();
            if (!StringUtils.hasText(expression)) {
                return;
            }

            long intervalMillis = Long.parseLong(prodEnvironment.resolvePlaceholders(expression));

            assertThat(Duration.ofMillis(intervalMillis))
                    .as("%s.%s (%s)", method.getDeclaringClass().getSimpleName(), method.getName(), expression)
                    .isGreaterThanOrEqualTo(MIN_PROD_INTERVAL);
        });
    }

    @Test
    void cronJobsShouldNotRunMoreOftenThanMinIntervalInProd() {
        scheduledMethods.forEach(method -> {
            var cron = method.getAnnotation(Scheduled.class).cron();
            if (!StringUtils.hasText(cron)) {
                return;
            }

            var resolvedCron = prodEnvironment.resolvePlaceholders(cron);

            assertThat(shortestGap(CronExpression.parse(resolvedCron)))
                    .as("%s.%s (%s)", method.getDeclaringClass().getSimpleName(), method.getName(), resolvedCron)
                    .isGreaterThanOrEqualTo(MIN_PROD_INTERVAL);
        });
    }

    @Test
    void frequentJobsShouldShareOneIntervalSoTheDatabaseWakesUpOnce() {
        var frequentIntervals = scheduledMethods.stream()
                .map(method -> method.getAnnotation(Scheduled.class).fixedRateString())
                .filter(StringUtils::hasText)
                .map(prodEnvironment::resolvePlaceholders)
                .distinct()
                .toList();

        assertThat(frequentIntervals).containsExactly(prodEnvironment.getProperty("scheduler.frequent-interval"));
    }

    private static Duration shortestGap(CronExpression cron) {
        var start = LocalDateTime.of(2026, 1, 5, 0, 0);
        var previous = cron.next(start);
        var shortest = Duration.ofDays(7);
        for (int i = 0; i < 200 && previous != null; i++) {
            var next = cron.next(previous);
            if (next == null) {
                break;
            }
            var gap = Duration.between(previous, next);
            if (gap.compareTo(shortest) < 0) {
                shortest = gap;
            }
            previous = next;
        }
        return shortest;
    }
}
