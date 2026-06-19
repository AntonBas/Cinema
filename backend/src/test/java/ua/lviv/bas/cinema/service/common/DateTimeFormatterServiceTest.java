package ua.lviv.bas.cinema.service.common;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class DateTimeFormatterServiceTest {

    private final DateTimeFormatterService dateTimeFormatterService = new DateTimeFormatterService();

    @Test
    void formatStandardWhenDateTimeNotNullShouldReturnFormattedString() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30);
        String result = dateTimeFormatterService.formatStandard(dateTime);
        assertThat(result).isEqualTo("15.01.2024 14:30");
    }

    @Test
    void formatStandardWhenDateTimeNullShouldReturnEmptyString() {
        String result = dateTimeFormatterService.formatStandard(null);
        assertThat(result).isEmpty();
    }
}