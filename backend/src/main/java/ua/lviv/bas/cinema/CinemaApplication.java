package ua.lviv.bas.cinema;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CinemaApplication {

	private static final String CINEMA_TIME_ZONE = "Europe/Kyiv";

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone(CINEMA_TIME_ZONE));
		SpringApplication.run(CinemaApplication.class, args);
	}
}
