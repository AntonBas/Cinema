package ua.lviv.bas.cinema.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class CinemaTime {

    public static final String ZONE_ID = "Europe/Kyiv";
    public static final ZoneId ZONE = ZoneId.of(ZONE_ID);

    private CinemaTime() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }
}
