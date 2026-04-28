package ua.lviv.bas.cinema.service.common;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateTimeFormatterService {

    private static final DateTimeFormatter STANDARD_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public String formatStandard(LocalDateTime dateTime) {
        if (dateTime == null)
            return "";
        return dateTime.format(STANDARD_FORMATTER);
    }
}