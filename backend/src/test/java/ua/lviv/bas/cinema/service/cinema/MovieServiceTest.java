package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
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
import org.springframework.web.multipart.MultipartFile;

import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Person;
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
		when(posterService.getPosterUrl(any(), any())).thenReturn("poster-url");

		MovieDetailResponse result = movieService.createMovie(request);

		assertThat(result.getTitle()).isEqualTo("New Movie");
		verify(movieRepository).save(movie);
		verify(movieMapper).toMovie(request);
	}

	@Test
	void createMovie_WhenEndDateBeforeReleaseDate_ShouldThrowException() {
		MovieCreateRequest request = MovieCreateRequest.builder().title("Invalid Movie")
				.releaseDate(LocalDate.now().plusDays(10)).endShowingDate(LocalDate.now().plusDays(5)).build();

		assertThatThrownBy(() -> movieService.createMovie(request)).isInstanceOf(MovieValidationException.class);

		verify(movieRepository, never()).save(any());
	}

	@Test
	void createMovie_WhenSlugExists_ShouldThrowException() {
		MovieCreateRequest request = MovieCreateRequest.builder().title("New Movie")
				.releaseDate(LocalDate.now().plusDays(1)).endShowingDate(LocalDate.now().plusDays(30)).build();

		when(slugService.generateUniqueSlug("New Movie")).thenReturn("existing-movie");
		when(movieRepository.findBySlug("existing-movie")).thenReturn(Optional.of(new Movie()));

		assertThatThrownBy(() -> movieService.createMovie(request)).isInstanceOf(DuplicateEntityException.class);

		verify(movieRepository, never()).save(any());
	}

	@Test
	void createMovie_WithRelations_Success() {
		MovieCreateRequest request = MovieCreateRequest.builder().title("Movie with Relations")
				.releaseDate(LocalDate.now().plusDays(1)).endShowingDate(LocalDate.now().plusDays(30))
				.genreIds(List.of(1L, 2L)).actorIds(List.of(3L, 4L)).directorIds(List.of(5L))
				.screenwriterIds(List.of(6L)).build();

		Movie movie = new Movie();
		movie.setId(1L);

		MovieDetailResponse response = MovieDetailResponse.builder().id(1L).build();

		Genre genre1 = new Genre();
		genre1.setId(1L);
		Genre genre2 = new Genre();
		genre2.setId(2L);
		Person actor1 = new Person();
		actor1.setId(3L);
		Person actor2 = new Person();
		actor2.setId(4L);
		Person director = new Person();
		director.setId(5L);
		Person screenwriter = new Person();
		screenwriter.setId(6L);

		when(slugService.generateUniqueSlug("Movie with Relations")).thenReturn("movie-with-relations");
		when(movieRepository.findBySlug("movie-with-relations")).thenReturn(Optional.empty());
		when(movieMapper.toMovie(request)).thenReturn(movie);
		when(movieScheduler.calculateMovieStatus(movie, LocalDate.now())).thenReturn(MovieStatus.UPCOMING);
		when(genreRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(genre1, genre2));
		when(personRepository.findAllById(List.of(3L, 4L))).thenReturn(List.of(actor1, actor2));
		when(personRepository.findAllById(List.of(5L))).thenReturn(List.of(director));
		when(personRepository.findAllById(List.of(6L))).thenReturn(List.of(screenwriter));
		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toMovieDetailResponse(movie)).thenReturn(response);
		when(posterService.getPosterUrl(any(), any())).thenReturn("poster-url");

		movieService.createMovie(request);

		verify(genreRepository).findAllById(List.of(1L, 2L));
		verify(personRepository, times(3)).findAllById(any());
		verify(movieRepository).save(movie);
	}

	@Test
	void createMovie_WithPoster_Success() {
		MovieCreateRequest request = MovieCreateRequest.builder().title("Movie with Poster")
				.releaseDate(LocalDate.now().plusDays(1)).endShowingDate(LocalDate.now().plusDays(30))
				.posterFile(mock(MultipartFile.class)).build();

		Movie movie = new Movie();
		movie.setId(1L);

		MovieDetailResponse response = MovieDetailResponse.builder().id(1L).build();

		when(slugService.generateUniqueSlug("Movie with Poster")).thenReturn("movie-with-poster");
		when(movieRepository.findBySlug("movie-with-poster")).thenReturn(Optional.empty());
		when(movieMapper.toMovie(request)).thenReturn(movie);
		when(movieScheduler.calculateMovieStatus(movie, LocalDate.now())).thenReturn(MovieStatus.UPCOMING);
		when(posterService.uploadPoster(any())).thenReturn("uploaded-poster.jpg");
		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toMovieDetailResponse(movie)).thenReturn(response);
		when(posterService.getPosterUrl(any(), any())).thenReturn("poster-url");

		movieService.createMovie(request);

		verify(posterService).uploadPoster(any());
		assertThat(movie.getPosterFileName()).isEqualTo("uploaded-poster.jpg");
	}

	@Test
	void getMovieById_Success() {
		Movie movie = new Movie();
		movie.setId(1L);
		movie.setStatus(MovieStatus.CURRENT);

		MovieDetailResponse response = MovieDetailResponse.builder().id(1L).title("Movie Title").build();

		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(movieMapper.toMovieDetailResponse(movie)).thenReturn(response);
		when(posterService.getPosterUrl(any(), any())).thenReturn("poster-url");

		MovieDetailResponse result = movieService.getMovieById(1L);

		assertThat(result.getTitle()).isEqualTo("Movie Title");
		verify(movieRepository).findById(1L);
	}

	@Test
	void getMovieById_WhenNotFound_ShouldThrowException() {
		when(movieRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> movieService.getMovieById(1L)).isInstanceOf(MovieNotFoundException.class);

		verify(movieRepository).findById(1L);
	}

	@Test
	void updateMovie_Success() {
		Movie movie = new Movie();
		movie.setId(1L);
		movie.setTitle("Old Title");
		movie.setGenres(new HashSet<>());
		movie.setActors(new HashSet<>());
		movie.setDirectors(new HashSet<>());
		movie.setScreenwriters(new HashSet<>());

		MovieUpdateRequest request = MovieUpdateRequest.builder().title("New Title")
				.releaseDate(LocalDate.now().plusDays(1)).endShowingDate(LocalDate.now().plusDays(30)).build();

		MovieDetailResponse response = MovieDetailResponse.builder().id(1L).title("New Title").build();

		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(slugService.generateUniqueSlug("New Title")).thenReturn("new-title");
		when(slugService.isSlugAvailableForMovie("new-title", 1L)).thenReturn(true);
		when(movieScheduler.calculateMovieStatus(movie, LocalDate.now())).thenReturn(MovieStatus.UPCOMING);
		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toMovieDetailResponse(movie)).thenReturn(response);
		when(posterService.getPosterUrl(any(), any())).thenReturn("poster-url");

		MovieDetailResponse result = movieService.updateMovie(1L, request);

		assertThat(result.getTitle()).isEqualTo("New Title");
		verify(movieRepository).save(movie);
		assertThat(movie.getSlug()).isEqualTo("new-title");
	}

	@Test
	void updateMovie_WithSameTitle_ShouldNotChangeSlug() {
		Movie movie = new Movie();
		movie.setId(1L);
		movie.setTitle("Existing Title");
		movie.setSlug("existing-title");
		movie.setGenres(new HashSet<>());
		movie.setActors(new HashSet<>());
		movie.setDirectors(new HashSet<>());
		movie.setScreenwriters(new HashSet<>());

		MovieUpdateRequest request = MovieUpdateRequest.builder().title("Existing Title")
				.releaseDate(LocalDate.now().plusDays(1)).endShowingDate(LocalDate.now().plusDays(30)).build();

		MovieDetailResponse response = MovieDetailResponse.builder().id(1L).build();

		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(movieScheduler.calculateMovieStatus(movie, LocalDate.now())).thenReturn(MovieStatus.UPCOMING);
		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toMovieDetailResponse(movie)).thenReturn(response);
		when(posterService.getPosterUrl(any(), any())).thenReturn("poster-url");

		movieService.updateMovie(1L, request);

		verify(slugService, never()).generateUniqueSlug(anyString());
		verify(slugService, never()).isSlugAvailableForMovie(anyString(), any());
	}

	@Test
	void updateMovie_WhenSlugNotAvailable_ShouldThrowException() {
		Movie movie = new Movie();
		movie.setId(1L);
		movie.setTitle("Old Title");

		MovieUpdateRequest request = MovieUpdateRequest.builder().title("New Title")
				.releaseDate(LocalDate.now().plusDays(1)).endShowingDate(LocalDate.now().plusDays(30)).build();

		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(slugService.generateUniqueSlug("New Title")).thenReturn("existing-slug");
		when(slugService.isSlugAvailableForMovie("existing-slug", 1L)).thenReturn(false);

		assertThatThrownBy(() -> movieService.updateMovie(1L, request)).isInstanceOf(DuplicateEntityException.class);

		verify(movieRepository, never()).save(any());
	}

	@Test
	void updateMovie_WithPosterUpdate_Success() {
		Movie movie = new Movie();
		movie.setId(1L);
		movie.setTitle("Movie");
		movie.setPosterFileName("old-poster.jpg");
		movie.setGenres(new HashSet<>());
		movie.setActors(new HashSet<>());
		movie.setDirectors(new HashSet<>());
		movie.setScreenwriters(new HashSet<>());

		MultipartFile newPoster = mock(MultipartFile.class);
		MovieUpdateRequest request = MovieUpdateRequest.builder().title("Movie")
				.releaseDate(LocalDate.now().plusDays(1)).endShowingDate(LocalDate.now().plusDays(30))
				.posterFile(newPoster).build();

		MovieDetailResponse response = MovieDetailResponse.builder().id(1L).build();

		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(movieScheduler.calculateMovieStatus(movie, LocalDate.now())).thenReturn(MovieStatus.UPCOMING);
		when(posterService.uploadPoster(newPoster)).thenReturn("new-poster.jpg");
		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toMovieDetailResponse(movie)).thenReturn(response);
		when(posterService.getPosterUrl(any(), any())).thenReturn("poster-url");

		movieService.updateMovie(1L, request);

		verify(posterService).deletePoster("old-poster.jpg");
		verify(posterService).uploadPoster(newPoster);
		assertThat(movie.getPosterFileName()).isEqualTo("new-poster.jpg");
	}

	@Test
	void updateMovie_WithPosterRemoval_Success() {
		Movie movie = new Movie();
		movie.setId(1L);
		movie.setTitle("Movie");
		movie.setPosterFileName("old-poster.jpg");
		movie.setGenres(new HashSet<>());
		movie.setActors(new HashSet<>());
		movie.setDirectors(new HashSet<>());
		movie.setScreenwriters(new HashSet<>());

		MovieUpdateRequest request = MovieUpdateRequest.builder().title("Movie")
				.releaseDate(LocalDate.now().plusDays(1)).endShowingDate(LocalDate.now().plusDays(30))
				.removePoster(true).build();

		MovieDetailResponse response = MovieDetailResponse.builder().id(1L).build();

		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(movieScheduler.calculateMovieStatus(movie, LocalDate.now())).thenReturn(MovieStatus.UPCOMING);
		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toMovieDetailResponse(movie)).thenReturn(response);
		when(posterService.getPosterUrl(any(), any())).thenReturn("poster-url");

		movieService.updateMovie(1L, request);

		verify(posterService).deletePoster("old-poster.jpg");
		assertThat(movie.getPosterFileName()).isNull();
	}

	@Test
	void updateMovie_WithRelationsUpdate_Success() {
		Movie movie = new Movie();
		movie.setId(1L);
		movie.setTitle("Movie");
		movie.setGenres(new HashSet<>());
		movie.setActors(new HashSet<>());
		movie.setDirectors(new HashSet<>());
		movie.setScreenwriters(new HashSet<>());

		MovieUpdateRequest request = MovieUpdateRequest.builder().title("Movie")
				.releaseDate(LocalDate.now().plusDays(1)).endShowingDate(LocalDate.now().plusDays(30))
				.genreIds(List.of(1L)).actorIds(List.of(2L)).directorIds(List.of(3L)).screenwriterIds(List.of(4L))
				.build();

		Genre genre = new Genre();
		genre.setId(1L);
		Person actor = new Person();
		actor.setId(2L);
		Person director = new Person();
		director.setId(3L);
		Person screenwriter = new Person();
		screenwriter.setId(4L);

		MovieDetailResponse response = MovieDetailResponse.builder().id(1L).build();

		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(movieScheduler.calculateMovieStatus(movie, LocalDate.now())).thenReturn(MovieStatus.UPCOMING);
		when(genreRepository.findAllById(List.of(1L))).thenReturn(List.of(genre));
		when(personRepository.findAllById(List.of(2L))).thenReturn(List.of(actor));
		when(personRepository.findAllById(List.of(3L))).thenReturn(List.of(director));
		when(personRepository.findAllById(List.of(4L))).thenReturn(List.of(screenwriter));
		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toMovieDetailResponse(movie)).thenReturn(response);
		when(posterService.getPosterUrl(any(), any())).thenReturn("poster-url");

		movieService.updateMovie(1L, request);

		verify(genreRepository).findAllById(List.of(1L));
		verify(personRepository, times(3)).findAllById(any());
		assertThat(movie.getGenres()).hasSize(1);
		assertThat(movie.getActors()).hasSize(1);
		assertThat(movie.getDirectors()).hasSize(1);
		assertThat(movie.getScreenwriters()).hasSize(1);
	}

	@Test
	void deleteMovie_Success() {
		Movie movie = new Movie();
		movie.setId(1L);
		movie.setPosterFileName("poster.jpg");

		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		doNothing().when(posterService).deletePoster("poster.jpg");

		movieService.deleteMovie(1L);

		verify(posterService).deletePoster("poster.jpg");
		verify(movieRepository).delete(movie);
	}

	@Test
	void getMovieBySlug_Success() {
		Movie movie = new Movie();
		movie.setSlug("test-movie");
		movie.setStatus(MovieStatus.CURRENT);

		MovieDetailResponse response = MovieDetailResponse.builder().slug("test-movie").build();

		when(movieRepository.findBySlug("test-movie")).thenReturn(Optional.of(movie));
		when(movieMapper.toMovieDetailResponse(movie)).thenReturn(response);
		when(posterService.getPosterUrl(any(), any())).thenReturn("poster-url");

		MovieDetailResponse result = movieService.getMovieBySlug("test-movie");

		assertThat(result.getSlug()).isEqualTo("test-movie");
		verify(movieRepository).findBySlug("test-movie");
	}

	@Test
	void getMovieBySlug_WhenNotFound_ShouldThrowException() {
		when(movieRepository.findBySlug("non-existent")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> movieService.getMovieBySlug("non-existent"))
				.isInstanceOf(MovieNotFoundException.class);
	}

	@Test
	void findFilteredMovies_WithSearch_Success() {
		Movie movie = new Movie();
		MovieCardResponse response = MovieCardResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Movie> page = new PageImpl<>(Collections.singletonList(movie));

		when(movieRepository.findByStatusWithSearch(MovieStatus.CURRENT, "search", pageable)).thenReturn(page);
		when(movieMapper.toMovieCardResponse(movie)).thenReturn(response);
		when(posterService.getPosterUrl(any(), any())).thenReturn("poster-url");

		Page<MovieCardResponse> result = movieService.findFilteredMovies("search", MovieStatus.CURRENT, pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(movieRepository).findByStatusWithSearch(MovieStatus.CURRENT, "search", pageable);
	}

	@Test
	void findFilteredMovies_WithStatusOnly_Success() {
		Movie movie = new Movie();
		MovieCardResponse response = MovieCardResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Movie> page = new PageImpl<>(Collections.singletonList(movie));

		when(movieRepository.findByStatusWithSearch(MovieStatus.UPCOMING, null, pageable)).thenReturn(page);
		when(movieMapper.toMovieCardResponse(movie)).thenReturn(response);
		when(posterService.getPosterUrl(any(), any())).thenReturn("poster-url");

		Page<MovieCardResponse> result = movieService.findFilteredMovies(null, MovieStatus.UPCOMING, pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(movieRepository).findByStatusWithSearch(MovieStatus.UPCOMING, null, pageable);
	}

	@Test
	void findFilteredMovies_WithoutFilters_Success() {
		Movie movie = new Movie();
		MovieCardResponse response = MovieCardResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Movie> page = new PageImpl<>(Collections.singletonList(movie));

		when(movieRepository.findCurrentlyShowingPage(pageable)).thenReturn(page);
		when(movieMapper.toMovieCardResponse(movie)).thenReturn(response);
		when(posterService.getPosterUrl(any(), any())).thenReturn("poster-url");

		Page<MovieCardResponse> result = movieService.findFilteredMovies(null, null, pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(movieRepository).findCurrentlyShowingPage(pageable);
	}

	@Test
	void getMoviesByStatus_Success() {
		Movie movie = new Movie();
		MovieCardResponse response = MovieCardResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Movie> page = new PageImpl<>(Collections.singletonList(movie));

		when(movieRepository.findByStatusWithSearch(MovieStatus.UPCOMING, null, pageable)).thenReturn(page);
		when(movieMapper.toMovieCardResponse(movie)).thenReturn(response);
		when(posterService.getPosterUrl(any(), any())).thenReturn("poster-url");

		Page<MovieCardResponse> result = movieService.getMoviesByStatus(MovieStatus.UPCOMING, pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(movieRepository).findByStatusWithSearch(MovieStatus.UPCOMING, null, pageable);
	}

	@Test
	void searchMoviesByTitle_Success() {
		Movie movie = new Movie();
		MovieCardResponse response = MovieCardResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Movie> page = new PageImpl<>(Collections.singletonList(movie));

		when(movieRepository.findByStatusWithSearch(MovieStatus.CURRENT, "title", pageable)).thenReturn(page);
		when(movieMapper.toMovieCardResponse(movie)).thenReturn(response);
		when(posterService.getPosterUrl(any(), any())).thenReturn("poster-url");

		Page<MovieCardResponse> result = movieService.searchMoviesByTitle("title", MovieStatus.CURRENT, pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(movieRepository).findByStatusWithSearch(MovieStatus.CURRENT, "title", pageable);
	}

	@Test
	void getCurrentlyShowing_Success() {
		Movie movie = new Movie();
		movie.setStatus(MovieStatus.CURRENT);

		MovieCardResponse response = MovieCardResponse.builder().id(1L).currentlyShowing(true).build();

		Pageable pageable = PageRequest.of(0, 5, Sort.by("releaseDate").descending());

		when(movieRepository.findCurrentlyShowing(pageable)).thenReturn(Collections.singletonList(movie));
		when(movieMapper.toMovieCardResponse(movie)).thenReturn(response);
		when(posterService.getPosterUrl(any(), any())).thenReturn("poster-url");

		List<MovieCardResponse> result = movieService.getCurrentlyShowing(5);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).isCurrentlyShowing()).isTrue();
		verify(movieRepository).findCurrentlyShowing(pageable);
	}

	@Test
	void getCurrentlyShowingPage_Success() {
		Movie movie = new Movie();
		MovieCardResponse response = MovieCardResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Movie> page = new PageImpl<>(Collections.singletonList(movie));

		when(movieRepository.findCurrentlyShowingPage(pageable)).thenReturn(page);
		when(movieMapper.toMovieCardResponse(movie)).thenReturn(response);
		when(posterService.getPosterUrl(any(), any())).thenReturn("poster-url");

		Page<MovieCardResponse> result = movieService.getCurrentlyShowingPage(pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(movieRepository).findCurrentlyShowingPage(pageable);
	}

	@Test
	void getUpcoming_Success() {
		Movie movie = new Movie();
		movie.setStatus(MovieStatus.UPCOMING);

		MovieCardResponse response = MovieCardResponse.builder().id(1L).currentlyShowing(false).build();

		Pageable pageable = PageRequest.of(0, 5, Sort.by("releaseDate"));

		when(movieRepository.findUpcoming(pageable)).thenReturn(Collections.singletonList(movie));
		when(movieMapper.toMovieCardResponse(movie)).thenReturn(response);
		when(posterService.getPosterUrl(any(), any())).thenReturn("poster-url");

		List<MovieCardResponse> result = movieService.getUpcoming(5);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).isCurrentlyShowing()).isFalse();
		verify(movieRepository).findUpcoming(pageable);
	}

	@Test
	void getUpcomingPage_Success() {
		Movie movie = new Movie();
		MovieCardResponse response = MovieCardResponse.builder().id(1L).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Movie> page = new PageImpl<>(Collections.singletonList(movie));

		when(movieRepository.findUpcomingPage(pageable)).thenReturn(page);
		when(movieMapper.toMovieCardResponse(movie)).thenReturn(response);
		when(posterService.getPosterUrl(any(), any())).thenReturn("poster-url");

		Page<MovieCardResponse> result = movieService.getUpcomingPage(pageable);

		assertThat(result.getContent()).hasSize(1);
		verify(movieRepository).findUpcomingPage(pageable);
	}

	@Test
	void getArchivedMovies_Success() {
		Movie movie = new Movie();
		movie.setStatus(MovieStatus.ARCHIVED);

		MovieCardResponse response = MovieCardResponse.builder().id(1L).currentlyShowing(false).build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Movie> page = new PageImpl<>(Collections.singletonList(movie));

		when(movieRepository.findArchived(pageable)).thenReturn(page);
		when(movieMapper.toMovieCardResponse(movie)).thenReturn(response);
		when(posterService.getPosterUrl(any(), any())).thenReturn("poster-url");

		Page<MovieCardResponse> result = movieService.getArchivedMovies(pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).isCurrentlyShowing()).isFalse();
		verify(movieRepository).findArchived(pageable);
	}

	@Test
	void searchMoviesForSessionCreation_Success() {
		Movie movie = new Movie();
		movie.setId(1L);
		movie.setTitle("Test Movie");
		movie.setReleaseDate(LocalDate.now().plusDays(5));
		movie.setDurationMinutes(120);

		when(movieRepository.findMoviesForSessionCreation("test", LocalDate.now().plusDays(10)))
				.thenReturn(Collections.singletonList(movie));

		List<MovieSessionSearchResponse> result = movieService.searchMoviesForSessionCreation("test",
				LocalDate.now().plusDays(10));

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getTitle()).isEqualTo("Test Movie");
		assertThat(result.get(0).getDurationMinutes()).isEqualTo(120);
		verify(movieRepository).findMoviesForSessionCreation("test", LocalDate.now().plusDays(10));
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
		verify(movieRepository).findById(1L);
		verify(posterService).getPosterResponse("poster.jpg");
	}

	@Test
	void getMoviePoster_WithoutPoster_ShouldCallServiceWithNull() {
		Movie movie = new Movie();
		movie.setPosterFileName(null);

		ResponseEntity<byte[]> responseEntity = ResponseEntity.ok().build();

		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(posterService.getPosterResponse(null)).thenReturn(responseEntity);

		movieService.getMoviePoster(1L);

		verify(posterService).getPosterResponse(null);
	}

	private MultipartFile mock(Class<MultipartFile> class1) {
		return org.mockito.Mockito.mock(MultipartFile.class);
	}
}