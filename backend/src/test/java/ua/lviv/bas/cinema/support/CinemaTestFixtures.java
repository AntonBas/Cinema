package ua.lviv.bas.cinema.support;

import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.cinema.domain.Session;

import java.time.LocalDateTime;

public final class CinemaTestFixtures {

    private CinemaTestFixtures() {
    }

    public static Movie movie() {
        return Movie.builder().title("Test Movie").build();
    }

    public static CinemaHall hall() {
        return CinemaHall.builder().name("Hall A").build();
    }

    public static Session session(Movie movie, CinemaHall hall) {
        return Session.builder().movie(movie).hall(hall).startTime(LocalDateTime.now().plusHours(2)).build();
    }
}
