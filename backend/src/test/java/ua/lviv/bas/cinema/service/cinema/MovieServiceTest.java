package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieValidationException;
import ua.lviv.bas.cinema.mapper.MovieMapper;
import ua.lviv.bas.cinema.repository.GenreRepository;
import ua.lviv.bas.cinema.repository.MovieRepository;
import ua.lviv.bas.cinema.repository.PersonRepository;
import ua.lviv.bas.cinema.scheduler.MovieScheduler;
import ua.lviv.bas.cinema.service.infrastructure.PosterService;
import ua.lviv.bas.cinema.service.infrastructure.SlugService;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {

	@Mock
	private MovieRepository movieRepository;

	@Mock
	private GenreRepository genreRepository;

	@Mock
	private PersonRepository personRepository;

	@Mock
	private MovieMapper movieMapper;

	@Mock
	private SlugService slugService;

	@Mock
	private MovieScheduler movieScheduler;

	@Mock
	private PosterService posterService;

	@InjectMocks
	private MovieService movieService;

	@Test
	void createMovie_Success() {
		MovieCreateRequest request = MovieCreateRequest.builder().title("New Movie")
				.releaseDate(LocalDate.now().plusDays(1)).endShowingDate(LocalDate.now().plusDays(30)).build();

		Movie movie = new Movie();
		movie.setId(1L);
		movie.setTitle("New Movie");

		MovieDetailResponse response = MovieDetailResponse.builder().id(1L).title("New Movie").build();

		when(slugService.generateUniqueSlug("New Movie")).thenReturn("new-movie");
		when(movieRepository.findBySlug("new-movie")).thenReturn(Optional.empty());
		when(movieMapper.toMovie(request)).thenReturn(movie);
		when(movieScheduler.calculateMovieStatus(movie, LocalDate.now())).thenReturn(MovieStatus.UPCOMING);
		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toMovieDetailResponse(movie)).thenReturn(response);

		MovieDetailResponse result = movieService.createMovie(request);

		assertThat(result.getTitle()).isEqualTo("New Movie");
	}

	@Test
	void createMovie_WhenEndDateBeforeReleaseDate_ShouldThrowException() {
		MovieCreateRequest request = MovieCreateRequest.builder().title("Invalid Movie")
				.releaseDate(LocalDate.now().plusDays(10)).endShowingDate(LocalDate.now().plusDays(5)).build();

		assertThatThrownBy(() -> movieService.createMovie(request)).isInstanceOf(MovieValidationException.class);
	}

	@Test
	void createMovie_WhenSlugExists_ShouldThrowException() {
		MovieCreateRequest request = MovieCreateRequest.builder().title("New Movie")
				.releaseDate(LocalDate.now().plusDays(1)).endShowingDate(LocalDate.now().plusDays(30)).build();

		when(slugService.generateUniqueSlug("New Movie")).thenReturn("existing-movie");
		when(movieRepository.findBySlug("existing-movie")).thenReturn(Optional.of(new Movie()));

		assertThatThrownBy(() -> movieService.createMovie(request)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void getMovieById_Success() {
		Movie movie = new Movie();
		movie.setId(1L);

		MovieDetailResponse response = MovieDetailResponse.builder().id(1L).title("Movie Title").build();

		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(movieMapper.toMovieDetailResponse(movie)).thenReturn(response);

		MovieDetailResponse result = movieService.getMovieById(1L);

		assertThat(result.getTitle()).isEqualTo("Movie Title");
	}

	@Test
	void getMovieById_WhenNotFound_ShouldThrowException() {
		when(movieRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> movieService.getMovieById(1L)).isInstanceOf(MovieNotFoundException.class);
	}

	@Test
	void updateMovie_Success() {
		Movie movie = new Movie();
		movie.setId(1L);
		movie.setTitle("Old Title");

		MovieUpdateRequest request = MovieUpdateRequest.builder().title("New Title")
				.releaseDate(LocalDate.now().plusDays(1)).endShowingDate(LocalDate.now().plusDays(30)).build();

		MovieDetailResponse response = MovieDetailResponse.builder().id(1L).title("New Title").build();

		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(slugService.generateUniqueSlug("New Title")).thenReturn("new-title");
		when(slugService.isSlugAvailableForMovie("new-title", 1L)).thenReturn(true);
		when(movieScheduler.calculateMovieStatus(movie, LocalDate.now())).thenReturn(MovieStatus.UPCOMING);
		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toMovieDetailResponse(movie)).thenReturn(response);

		MovieDetailResponse result = movieService.updateMovie(1L, request);

		assertThat(result.getTitle()).isEqualTo("New Title");
	}

	@Test
	void deleteMovie_Success() {
		Movie movie = new Movie();
		movie.setId(1L);
		movie.setPosterFileName("poster.jpg");

		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		doNothing().when(posterService).deletePoster("poster.jpg");
		doNothing().when(movieRepository).delete(movie);

		movieService.deleteMovie(1L);

		verify(movieRepository).delete(movie);
	}

	@Test
	void getMovieBySlug_Success() {
		Movie movie = new Movie();
		movie.setSlug("test-movie");

		MovieDetailResponse response = MovieDetailResponse.builder().slug("test-movie").build();

		when(movieRepository.findBySlug("test-movie")).thenReturn(Optional.of(movie));
		when(movieMapper.toMovieDetailResponse(movie)).thenReturn(response);

		MovieDetailResponse result = movieService.getMovieBySlug("test-movie");

		assertThat(result.getSlug()).isEqualTo("test-movie");
	}

	@Test
	void getMoviesByStatus_Success() {
		Movie movie = new Movie();
		MovieCardResponse response = MovieCardResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Movie> page = new PageImpl<>(Collections.singletonList(movie));

		when(movieRepository.findByStatusWithSearch(MovieStatus.UPCOMING, null, pageable)).thenReturn(page);
		when(movieMapper.toMovieCardResponse(movie)).thenReturn(response);

		Page<MovieCardResponse> result = movieService.getMoviesByStatus(MovieStatus.UPCOMING, pageable);

		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void getCurrentlyShowing_Success() {
		Movie movie = new Movie();
		MovieCardResponse response = MovieCardResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 5, Sort.by("releaseDate").descending());

		when(movieRepository.findCurrentlyShowing(pageable)).thenReturn(Collections.singletonList(movie));
		when(movieMapper.toMovieCardResponseList(Collections.singletonList(movie)))
				.thenReturn(Collections.singletonList(response));

		List<MovieCardResponse> result = movieService.getCurrentlyShowing(5);

		assertThat(result).hasSize(1);
	}

	@Test
	void getUpcoming_Success() {
		Movie movie = new Movie();
		MovieCardResponse response = MovieCardResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 5, Sort.by("releaseDate"));

		when(movieRepository.findUpcoming(pageable)).thenReturn(Collections.singletonList(movie));
		when(movieMapper.toMovieCardResponseList(Collections.singletonList(movie)))
				.thenReturn(Collections.singletonList(response));

		List<MovieCardResponse> result = movieService.getUpcoming(5);

		assertThat(result).hasSize(1);
	}

	@Test
	void searchMoviesForSessionCreation_Success() {
		Movie movie = new Movie();
		movie.setId(1L);
		movie.setTitle("Test Movie");
		movie.setReleaseDate(LocalDate.now().plusDays(5));

		when(movieRepository.findMoviesForSessionCreation("test", LocalDate.now().plusDays(10)))
				.thenReturn(Collections.singletonList(movie));

		List<MovieSessionSearchResponse> result = movieService.searchMoviesForSessionCreation("test",
				LocalDate.now().plusDays(10));

		assertThat(result).hasSize(1);
	}

	@Test
	void getMoviePoster_Success() {
		Movie movie = new Movie();
		movie.setPosterFileName("poster.jpg");

		ResponseEntity<byte[]> responseEntity = ResponseEntity.ok().build();

		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(posterService.getPosterResponse("poster.jpg")).thenReturn(responseEntity);

		ResponseEntity<byte[]> result = movieService.getMoviePoster(1L);

		assertThat(result).isEqualTo(responseEntity);
	}
}