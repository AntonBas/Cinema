package ua.lviv.bas.cinema.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import ua.lviv.bas.cinema.domain.cinema.enums.AgeRating;
import ua.lviv.bas.cinema.domain.cinema.status.MovieStatus;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieAdminResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieNotFoundException;
import ua.lviv.bas.cinema.service.cinema.MovieService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminMovieControllerTest {

    @Mock
    private MovieService movieService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AdminMovieController movieController;

    private MovieAdminResponse createMovieAdminResponse(String title) {
        return new MovieAdminResponse(1L, title, "trailer.mp4", "Description", 120, LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30), AgeRating.PEGI_12, MovieStatus.UPCOMING, "poster.jpg",
                "/api/movies/" + 1L + "/poster", List.of(), List.of(), List.of(), List.of());
    }

    private MovieCardResponse createMovieCardDto(Long id, String title) {
        return new MovieCardResponse(id, title.toLowerCase().replace(" ", "-"), title, "/api/movies/" + id + "/poster",
                120, AgeRating.PEGI_12, MovieStatus.UPCOMING);
    }

    @Test
    void createMovieShouldReturnCreatedMovie() throws Exception {
        String movieDataJson = "{\"title\":\"New Movie\",\"description\":\"Description\",\"durationMinutes\":120}";
        MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg",
                "content".getBytes());
        MovieAdminResponse responseDto = createMovieAdminResponse("New Movie");

        MovieCreateRequest request = MovieCreateRequest.builder().title("New Movie").description("Description")
                .durationMinutes(120).build();

        when(objectMapper.readValue(movieDataJson, MovieCreateRequest.class)).thenReturn(request);
        when(movieService.createMovie(any(MovieCreateRequest.class))).thenReturn(responseDto);

        MovieAdminResponse response = movieController.createMovie(movieDataJson, posterFile);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("New Movie");
        verify(movieService).createMovie(any(MovieCreateRequest.class));
    }

    @Test
    void createMovieWithInvalidJsonShouldThrowException() throws Exception {
        String invalidMovieDataJson = "invalid json";
        MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg",
                "content".getBytes());

        when(objectMapper.readValue(invalidMovieDataJson, MovieCreateRequest.class))
                .thenThrow(new JsonProcessingException("Invalid JSON") {
                });

        assertThrows(IllegalArgumentException.class,
                () -> movieController.createMovie(invalidMovieDataJson, posterFile));
    }

    @Test
    void getMovieShouldReturnMovie() {
        MovieAdminResponse responseDto = createMovieAdminResponse("Test Movie");

        when(movieService.getMovie(1L)).thenReturn(responseDto);

        MovieAdminResponse response = movieController.getMovie(1L);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Test Movie");
        verify(movieService).getMovie(1L);
    }

    @Test
    void getMovieWhenNotFoundShouldThrowException() {
        when(movieService.getMovie(999L)).thenThrow(new MovieNotFoundException(999L));

        assertThrows(MovieNotFoundException.class, () -> movieController.getMovie(999L));
        verify(movieService).getMovie(999L);
    }

    @Test
    void getMoviesWithoutFiltersShouldReturnPageOfMovies() {
        Pageable pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.ASC, "title"));
        MovieCardResponse movie1 = createMovieCardDto(1L, "Movie 1");
        MovieCardResponse movie2 = createMovieCardDto(2L, "Movie 2");
        Page<MovieCardResponse> page = new PageImpl<>(List.of(movie1, movie2), pageable, 2);

        when(movieService.getMovies(isNull(), isNull(), eq(pageable))).thenReturn(page);

        PageResponse<MovieCardResponse> response = movieController.getMovies(null, null, pageable);

        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(2);
        assertThat(response.number()).isZero();
        assertThat(response.size()).isEqualTo(12);
        assertThat(response.totalElements()).isEqualTo(2);

        verify(movieService).getMovies(isNull(), isNull(), eq(pageable));
    }

    @Test
    void getMoviesWithQueryFilterShouldReturnFilteredMovies() {
        String queryFilter = "Movie";
        Pageable pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.ASC, "title"));
        MovieCardResponse movie = createMovieCardDto(1L, "Movie 1");
        Page<MovieCardResponse> page = new PageImpl<>(List.of(movie), pageable, 1);

        when(movieService.getMovies(eq(queryFilter), isNull(), eq(pageable))).thenReturn(page);

        PageResponse<MovieCardResponse> response = movieController.getMovies(queryFilter, null, pageable);

        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst().title()).isEqualTo("Movie 1");

        verify(movieService).getMovies(eq(queryFilter), isNull(), eq(pageable));
    }

    @Test
    void getMoviesWithStatusFilterShouldReturnFilteredMovies() {
        MovieStatus statusFilter = MovieStatus.UPCOMING;
        Pageable pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.ASC, "title"));
        MovieCardResponse movie = createMovieCardDto(1L, "Movie 1");
        Page<MovieCardResponse> page = new PageImpl<>(List.of(movie), pageable, 1);

        when(movieService.getMovies(isNull(), eq(statusFilter), eq(pageable))).thenReturn(page);

        PageResponse<MovieCardResponse> response = movieController.getMovies(null, statusFilter, pageable);

        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst().status()).isEqualTo(MovieStatus.UPCOMING);

        verify(movieService).getMovies(isNull(), eq(statusFilter), eq(pageable));
    }

    @Test
    void getMoviesWithBothFiltersShouldReturnFilteredMovies() {
        String queryFilter = "Movie";
        MovieStatus statusFilter = MovieStatus.UPCOMING;
        Pageable pageable = PageRequest.of(0, 12, Sort.by(Sort.Direction.ASC, "title"));
        MovieCardResponse movie = createMovieCardDto(1L, "Movie 1");
        Page<MovieCardResponse> page = new PageImpl<>(List.of(movie), pageable, 1);

        when(movieService.getMovies(eq(queryFilter), eq(statusFilter), eq(pageable))).thenReturn(page);

        PageResponse<MovieCardResponse> response = movieController.getMovies(queryFilter, statusFilter, pageable);

        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst().title()).isEqualTo("Movie 1");
        assertThat(response.content().getFirst().status()).isEqualTo(MovieStatus.UPCOMING);

        verify(movieService).getMovies(eq(queryFilter), eq(statusFilter), eq(pageable));
    }

    @Test
    void updateMovieShouldReturnUpdatedMovie() throws Exception {
        String movieDataJson = "{\"title\":\"Updated Movie\",\"description\":\"Updated Description\",\"durationMinutes\":130,\"removePoster\":false}";
        MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg",
                "content".getBytes());
        MovieAdminResponse responseDto = createMovieAdminResponse("Updated Movie");

        MovieUpdateRequest request = MovieUpdateRequest.builder().title("Updated Movie")
                .description("Updated Description").durationMinutes(130).removePoster(false).build();

        when(objectMapper.readValue(movieDataJson, MovieUpdateRequest.class)).thenReturn(request);
        when(movieService.updateMovie(eq(1L), any(MovieUpdateRequest.class))).thenReturn(responseDto);

        MovieAdminResponse response = movieController.updateMovie(1L, movieDataJson, posterFile);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Updated Movie");
        verify(movieService).updateMovie(eq(1L), any(MovieUpdateRequest.class));
    }

    @Test
    void updateMovieWithoutPosterShouldReturnUpdatedMovie() throws Exception {
        String movieDataJson = "{\"title\":\"Updated Movie\",\"description\":\"Updated Description\",\"durationMinutes\":130,\"removePoster\":true}";
        MovieAdminResponse responseDto = createMovieAdminResponse("Updated Movie");

        MovieUpdateRequest request = MovieUpdateRequest.builder().title("Updated Movie")
                .description("Updated Description").durationMinutes(130).removePoster(true).build();

        when(objectMapper.readValue(movieDataJson, MovieUpdateRequest.class)).thenReturn(request);
        when(movieService.updateMovie(eq(1L), any(MovieUpdateRequest.class))).thenReturn(responseDto);

        MovieAdminResponse response = movieController.updateMovie(1L, movieDataJson, null);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Updated Movie");
        verify(movieService).updateMovie(eq(1L), any(MovieUpdateRequest.class));
    }

    @Test
    void updateMovieWithInvalidJsonShouldThrowException() throws Exception {
        String invalidMovieDataJson = "invalid json";
        MockMultipartFile posterFile = new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg",
                "content".getBytes());

        when(objectMapper.readValue(invalidMovieDataJson, MovieUpdateRequest.class))
                .thenThrow(new JsonProcessingException("Invalid JSON") {
                });

        assertThrows(IllegalArgumentException.class,
                () -> movieController.updateMovie(1L, invalidMovieDataJson, posterFile));
    }

    @Test
    void deleteMovieShouldCallService() {
        movieController.deleteMovie(1L);

        verify(movieService).deleteMovie(1L);
    }

    @Test
    void deleteMovieWhenNotFoundShouldThrowException() {
        doThrow(new MovieNotFoundException(999L)).when(movieService).deleteMovie(999L);

        assertThrows(MovieNotFoundException.class, () -> movieController.deleteMovie(999L));
    }
}