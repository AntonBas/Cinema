package ua.lviv.bas.cinema.movie.service;

import org.junit.jupiter.api.Test;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.movie.domain.status.MovieStatus;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class MovieStatusCalculatorTest {

    private final MovieStatusCalculator calculator = new MovieStatusCalculator();

    @Test
    void calculateWhenMovieIsNullShouldReturnUnknown() {
        MovieStatus result = calculator.calculate(null, LocalDate.now());

        assertThat(result).isEqualTo(MovieStatus.UNKNOWN);
    }

    @Test
    void calculateWhenReleaseDateIsNullShouldReturnUnknown() {
        Movie movie = Movie.builder().releaseDate(null).build();

        MovieStatus result = calculator.calculate(movie, LocalDate.now());

        assertThat(result).isEqualTo(MovieStatus.UNKNOWN);
    }

    @Test
    void calculateWhenReferenceDateBeforeReleaseDateShouldReturnUpcoming() {
        Movie movie = Movie.builder().releaseDate(LocalDate.now().plusDays(5)).build();

        MovieStatus result = calculator.calculate(movie, LocalDate.now());

        assertThat(result).isEqualTo(MovieStatus.UPCOMING);
    }

    @Test
    void calculateWhenReferenceDateAfterEndShowingDateShouldReturnArchived() {
        Movie movie = Movie.builder()
                .releaseDate(LocalDate.now().minusDays(30))
                .endShowingDate(LocalDate.now().minusDays(1))
                .build();

        MovieStatus result = calculator.calculate(movie, LocalDate.now());

        assertThat(result).isEqualTo(MovieStatus.ARCHIVED);
    }

    @Test
    void calculateWhenWithinShowingPeriodShouldReturnCurrent() {
        Movie movie = Movie.builder()
                .releaseDate(LocalDate.now().minusDays(5))
                .endShowingDate(LocalDate.now().plusDays(5))
                .build();

        MovieStatus result = calculator.calculate(movie, LocalDate.now());

        assertThat(result).isEqualTo(MovieStatus.CURRENT);
    }

    @Test
    void calculateWhenEndShowingDateIsNullAndReleasedShouldReturnCurrent() {
        Movie movie = Movie.builder()
                .releaseDate(LocalDate.now().minusDays(5))
                .endShowingDate(null)
                .build();

        MovieStatus result = calculator.calculate(movie, LocalDate.now());

        assertThat(result).isEqualTo(MovieStatus.CURRENT);
    }

    @Test
    void calculateWhenReferenceDateEqualsReleaseDateShouldReturnCurrent() {
        LocalDate releaseDate = LocalDate.now();
        Movie movie = Movie.builder().releaseDate(releaseDate).build();

        MovieStatus result = calculator.calculate(movie, releaseDate);

        assertThat(result).isEqualTo(MovieStatus.CURRENT);
    }

    @Test
    void calculateWhenReferenceDateEqualsEndShowingDateShouldReturnCurrent() {
        LocalDate endShowingDate = LocalDate.now();
        Movie movie = Movie.builder()
                .releaseDate(LocalDate.now().minusDays(5))
                .endShowingDate(endShowingDate)
                .build();

        MovieStatus result = calculator.calculate(movie, endShowingDate);

        assertThat(result).isEqualTo(MovieStatus.CURRENT);
    }
}
