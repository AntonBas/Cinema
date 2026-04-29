package ua.lviv.bas.cinema.controller.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ua.lviv.bas.cinema.domain.cinema.enums.AgeRating;
import ua.lviv.bas.cinema.domain.cinema.status.MovieStatus;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieNotFoundException;
import ua.lviv.bas.cinema.service.cinema.MovieService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MovieControllerTest {

    @Mock
    private MovieService movieService;
    @InjectMocks
    private MovieController movieController;

    private final Long MOVIE_ID = 1L;
    private final String SLUG = "test-movie";

    private MovieDetailResponse createMovieDetailResponse(MovieStatus status) {
        String TITLE = "Test Movie";
        return new MovieDetailResponse(MOVIE_ID, TITLE, SLUG, "https://trailer.url", "Description", 120,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(30), AgeRating.PEGI_12, status, "poster.jpg",
                "/api/movies/" + MOVIE_ID + "/poster", List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private MovieCardResponse createMovieCardResponse(Long id, String title, MovieStatus status) {
        return new MovieCardResponse(id, "slug-" + id, title, "/api/movies/" + id + "/poster", 120, AgeRating.PEGI_12,
                status);
    }

    @Test
    void getMovieBySlugWhenMovieNotArchivedShouldReturnOk() {
        MovieDetailResponse movie = createMovieDetailResponse(MovieStatus.UPCOMING);

        when(movieService.getMovieBySlug(SLUG)).thenReturn(movie);

        ResponseEntity<MovieDetailResponse> response = movieController.getMovieBySlug(SLUG);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().slug()).isEqualTo(SLUG);
        verify(movieService).getMovieBySlug(SLUG);
    }

    @Test
    void getMovieBySlugWhenArchivedShouldThrowException() {
        MovieDetailResponse movie = createMovieDetailResponse(MovieStatus.ARCHIVED);

        when(movieService.getMovieBySlug(SLUG)).thenReturn(movie);

        assertThatThrownBy(() -> movieController.getMovieBySlug(SLUG)).isInstanceOf(MovieNotFoundException.class);
        verify(movieService).getMovieBySlug(SLUG);
    }

    @Test
    void getMovieBySlugWhenNotFoundShouldThrowException() {
        when(movieService.getMovieBySlug(SLUG)).thenThrow(new MovieNotFoundException(SLUG));

        assertThatThrownBy(() -> movieController.getMovieBySlug(SLUG)).isInstanceOf(MovieNotFoundException.class);
    }

    @Test
    void getCurrentlyShowingMoviesShouldReturnOk() {
        Pageable pageable = PageRequest.of(0, 12);
        MovieCardResponse movie1 = createMovieCardResponse(1L, "Movie 1", MovieStatus.CURRENT);
        MovieCardResponse movie2 = createMovieCardResponse(2L, "Movie 2", MovieStatus.CURRENT);
        Page<MovieCardResponse> page = new PageImpl<>(List.of(movie1, movie2), pageable, 2);

        when(movieService.getMovies(isNull(), eq(MovieStatus.CURRENT), eq(pageable))).thenReturn(page);

        ResponseEntity<PageResponse<MovieCardResponse>> response = movieController.getCurrentlyShowingMovies(pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content()).hasSize(2);
        verify(movieService).getMovies(isNull(), eq(MovieStatus.CURRENT), eq(pageable));
    }

    @Test
    void getUpcomingMoviesShouldReturnOk() {
        Pageable pageable = PageRequest.of(0, 12);
        MovieCardResponse movie1 = createMovieCardResponse(1L, "Movie 1", MovieStatus.UPCOMING);
        MovieCardResponse movie2 = createMovieCardResponse(2L, "Movie 2", MovieStatus.UPCOMING);
        Page<MovieCardResponse> page = new PageImpl<>(List.of(movie1, movie2), pageable, 2);

        when(movieService.getMovies(isNull(), eq(MovieStatus.UPCOMING), eq(pageable))).thenReturn(page);

        ResponseEntity<PageResponse<MovieCardResponse>> response = movieController.getUpcomingMovies(pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content()).hasSize(2);
        verify(movieService).getMovies(isNull(), eq(MovieStatus.UPCOMING), eq(pageable));
    }

    @Test
    void getCurrentMoviesForHomeShouldReturnOk() {
        MovieCardResponse movie1 = createMovieCardResponse(1L, "Movie 1", MovieStatus.CURRENT);
        MovieCardResponse movie2 = createMovieCardResponse(2L, "Movie 2", MovieStatus.CURRENT);
        List<MovieCardResponse> movies = List.of(movie1, movie2);

        when(movieService.getCurrentMovies(any())).thenReturn(movies);

        ResponseEntity<List<MovieCardResponse>> response = movieController.getCurrentMoviesForHome();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        verify(movieService).getCurrentMovies(any());
    }

    @Test
    void getUpcomingMoviesForHomeShouldReturnOk() {
        MovieCardResponse movie1 = createMovieCardResponse(1L, "Movie 1", MovieStatus.UPCOMING);
        MovieCardResponse movie2 = createMovieCardResponse(2L, "Movie 2", MovieStatus.UPCOMING);
        List<MovieCardResponse> movies = List.of(movie1, movie2);

        when(movieService.getUpcomingMovies(any())).thenReturn(movies);

        ResponseEntity<List<MovieCardResponse>> response = movieController.getUpcomingMoviesForHome();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        verify(movieService).getUpcomingMovies(any());
    }

    @Test
    void getLeavingSoonMoviesForHomeShouldReturnOk() {
        MovieCardResponse movie1 = createMovieCardResponse(1L, "Movie 1", MovieStatus.CURRENT);
        MovieCardResponse movie2 = createMovieCardResponse(2L, "Movie 2", MovieStatus.CURRENT);
        List<MovieCardResponse> movies = List.of(movie1, movie2);

        when(movieService.getLeavingSoonMovies(any())).thenReturn(movies);

        ResponseEntity<List<MovieCardResponse>> response = movieController.getLeavingSoonMoviesForHome();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        verify(movieService).getLeavingSoonMovies(any());
    }

    @Test
    void getPosterShouldReturnPoster() {
        byte[] posterData = new byte[]{1, 2, 3, 4, 5};
        ResponseEntity<byte[]> expectedResponse = ResponseEntity.ok(posterData);

        when(movieService.getPoster(MOVIE_ID)).thenReturn(expectedResponse);

        ResponseEntity<byte[]> response = movieController.getPoster(MOVIE_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(posterData);
        verify(movieService).getPoster(MOVIE_ID);
    }

    @Test
    void getPosterWhenNotFoundShouldThrowException() {
        when(movieService.getPoster(MOVIE_ID)).thenThrow(new MovieNotFoundException(MOVIE_ID));

        assertThatThrownBy(() -> movieController.getPoster(MOVIE_ID)).isInstanceOf(MovieNotFoundException.class);
    }
}