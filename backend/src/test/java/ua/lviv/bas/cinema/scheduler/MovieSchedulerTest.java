package ua.lviv.bas.cinema.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.status.MovieStatus;
import ua.lviv.bas.cinema.repository.cinema.MovieRepository;
import ua.lviv.bas.cinema.service.cinema.MovieStatusCalculator;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieSchedulerTest {

    @Mock
    private MovieRepository movieRepository;
    @Mock
    private MovieStatusCalculator movieStatusCalculator;

    @InjectMocks
    private MovieScheduler movieScheduler;

    @Test
    void updateMovieStatusesWhenNoStatusChangedShouldNotSave() {
        var movie = Movie.builder().id(1L).title("Test Movie").status(MovieStatus.CURRENT).build();

        when(movieRepository.findAll()).thenReturn(List.of(movie));
        when(movieStatusCalculator.calculate(eq(movie), any(LocalDate.class))).thenReturn(MovieStatus.CURRENT);

        movieScheduler.updateMovieStatuses();

        verify(movieRepository, never()).saveAll(any());
    }

    @Test
    void updateMovieStatusesShouldUpdateChangedStatusesAndSave() {
        var movie = Movie.builder().id(1L).title("Test Movie").status(MovieStatus.UPCOMING).build();

        when(movieRepository.findAll()).thenReturn(List.of(movie));
        when(movieStatusCalculator.calculate(eq(movie), any(LocalDate.class))).thenReturn(MovieStatus.CURRENT);

        movieScheduler.updateMovieStatuses();

        assertThat(movie.getStatus()).isEqualTo(MovieStatus.CURRENT);
        verify(movieRepository).saveAll(List.of(movie));
    }
}
