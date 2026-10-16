package ua.lviv.bas.cinema.movie.movie;

import org.springframework.stereotype.Component;
import ua.lviv.bas.cinema.movie.movie.Movie;
import ua.lviv.bas.cinema.movie.movie.MovieStatus;

import java.time.LocalDate;

@Component
public class MovieStatusCalculator {

    public MovieStatus calculate(Movie movie, LocalDate referenceDate) {
        if (movie == null || movie.getReleaseDate() == null) {
            return MovieStatus.UNKNOWN;
        }

        if (referenceDate.isBefore(movie.getReleaseDate())) {
            return MovieStatus.UPCOMING;
        } else if (movie.getEndShowingDate() != null && referenceDate.isAfter(movie.getEndShowingDate())) {
            return MovieStatus.ARCHIVED;
        } else {
            return MovieStatus.CURRENT;
        }
    }
}
