package ua.lviv.bas.cinema.movie.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.movie.domain.status.MovieStatus;
import ua.lviv.bas.cinema.movie.repository.MovieRepository;
import ua.lviv.bas.cinema.movie.service.MovieStatusCalculator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    @Mock
    private PlatformTransactionManager transactionManager;

    @InjectMocks
    private MovieScheduler movieScheduler;

    @Test
    void updateMovieStatusesWhenNoStatusChangedShouldNotSave() {
        var movie = Movie.builder().id(1L).title("Test Movie").status(MovieStatus.CURRENT).build();

        when(movieRepository.findAll()).thenReturn(List.of(movie));
        when(movieStatusCalculator.calculate(eq(movie), any(LocalDate.class))).thenReturn(MovieStatus.CURRENT);

        movieScheduler.updateMovieStatuses();

        verify(movieRepository, never()).save(any());
    }

    @Test
    void updateMovieStatusesShouldUpdateChangedStatusesAndSave() {
        var movie = Movie.builder().id(1L).title("Test Movie").status(MovieStatus.UPCOMING).build();

        when(movieRepository.findAll()).thenReturn(List.of(movie));
        when(movieStatusCalculator.calculate(eq(movie), any(LocalDate.class))).thenReturn(MovieStatus.CURRENT);
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        movieScheduler.updateMovieStatuses();

        assertThat(movie.getStatus()).isEqualTo(MovieStatus.CURRENT);
        verify(movieRepository).save(movie);
    }

    @Test
    void updateMovieStatusesShouldSkipMovieThatWasConcurrentlyModified() {
        var conflictingMovie = Movie.builder().id(1L).title("Conflicting Movie").status(MovieStatus.UPCOMING).build();
        var okMovie = Movie.builder().id(2L).title("OK Movie").status(MovieStatus.UPCOMING).build();

        when(movieRepository.findAll()).thenReturn(List.of(conflictingMovie, okMovie));
        when(movieStatusCalculator.calculate(any(Movie.class), any(LocalDate.class))).thenReturn(MovieStatus.CURRENT);
        when(movieRepository.findById(1L)).thenReturn(Optional.of(conflictingMovie));
        when(movieRepository.findById(2L)).thenReturn(Optional.of(okMovie));
        doThrow(new ObjectOptimisticLockingFailureException(Movie.class, 1L)).when(movieRepository)
                .save(conflictingMovie);

        movieScheduler.updateMovieStatuses();

        verify(movieRepository).save(okMovie);
        assertThat(okMovie.getStatus()).isEqualTo(MovieStatus.CURRENT);
    }
}
