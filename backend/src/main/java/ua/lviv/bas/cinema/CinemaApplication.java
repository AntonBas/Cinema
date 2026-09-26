package ua.lviv.bas.cinema;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ua.lviv.bas.cinema.common.CinemaTime;

@SpringBootApplication
public class CinemaApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(CinemaTime.ZONE));
        SpringApplication.run(CinemaApplication.class, args);
    }
}
