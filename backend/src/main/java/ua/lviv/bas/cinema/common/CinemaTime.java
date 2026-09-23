package ua.lviv.bas.cinema.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class CinemaTime {

    public static final ZoneId ZONE = ZoneId.of("Europe/Kyiv");

    private CinemaTime() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }
}
