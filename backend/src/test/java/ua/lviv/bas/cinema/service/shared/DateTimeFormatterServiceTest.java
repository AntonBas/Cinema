package ua.lviv.bas.cinema.service.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DateTimeFormatterServiceTest {

	private final DateTimeFormatterService dateTimeFormatterService = new DateTimeFormatterService();

	@Test
	void formatStandard_WhenDateTimeNotNull_ShouldReturnFormattedString() {
		LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30);
		String result = dateTimeFormatterService.formatStandard(dateTime);
		assertThat(result).isEqualTo("15.01.2024 14:30");
	}

	@Test
	void formatStandard_WhenDateTimeNull_ShouldReturnEmptyString() {
		String result = dateTimeFormatterService.formatStandard(null);
		assertThat(result).isEmpty();
	}

	@Test
	void formatDateOnly_WhenDateTimeNotNull_ShouldReturnFormattedString() {
		LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30);
		String result = dateTimeFormatterService.formatDateOnly(dateTime);
		assertThat(result).isEqualTo("15.01.2024");
	}

	@Test
	void formatDateOnly_WhenDateTimeNull_ShouldReturnEmptyString() {
		String result = dateTimeFormatterService.formatDateOnly(null);
		assertThat(result).isEmpty();
	}

	@Test
	void formatTimeOnly_WhenDateTimeNotNull_ShouldReturnFormattedString() {
		LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30);
		String result = dateTimeFormatterService.formatTimeOnly(dateTime);
		assertThat(result).isEqualTo("14:30");
	}

	@Test
	void formatTimeOnly_WhenDateTimeNull_ShouldReturnEmptyString() {
		String result = dateTimeFormatterService.formatTimeOnly(null);
		assertThat(result).isEmpty();
	}

	@Test
	void formatDateTime_WhenDateTimeNotNull_ShouldReturnFormattedString() {
		LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30);
		String result = dateTimeFormatterService.formatDateTime(dateTime, "yyyy-MM-dd HH:mm:ss");
		assertThat(result).isEqualTo("2024-01-15 14:30:00");
	}

	@Test
	void formatDateTime_WhenDateTimeNull_ShouldReturnEmptyString() {
		String result = dateTimeFormatterService.formatDateTime(null, "yyyy-MM-dd");
		assertThat(result).isEmpty();
	}
}