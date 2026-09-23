package ua.lviv.bas.cinema;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import ua.lviv.bas.cinema.common.CinemaTime;

@EnableScheduling
@SpringBootApplication
public class CinemaApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone(CinemaTime.ZONE));
		SpringApplication.run(CinemaApplication.class, args);
	}
}
