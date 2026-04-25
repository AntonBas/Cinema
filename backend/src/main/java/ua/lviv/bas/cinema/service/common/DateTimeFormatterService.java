package ua.lviv.bas.cinema.service.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

@Component
public class DateTimeFormatterService {

	private static final DateTimeFormatter STANDARD_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
	private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
	private static final DateTimeFormatter TIME_ONLY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

	public String formatStandard(LocalDateTime dateTime) {
		if (dateTime == null)
			return "";
		return dateTime.format(STANDARD_FORMATTER);
	}

	public String formatDateOnly(LocalDateTime dateTime) {
		if (dateTime == null)
			return "";
		return dateTime.format(DATE_ONLY_FORMATTER);
	}

	public String formatTimeOnly(LocalDateTime dateTime) {
		if (dateTime == null)
			return "";
		return dateTime.format(TIME_ONLY_FORMATTER);
	}

	public String formatDateTime(LocalDateTime dateTime, String pattern) {
		if (dateTime == null)
			return "";
		return dateTime.format(DateTimeFormatter.ofPattern(pattern));
	}
}