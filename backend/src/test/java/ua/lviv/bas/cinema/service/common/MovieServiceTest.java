package ua.lviv.bas.cinema.service.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieNotFoundException;
import ua.lviv.bas.cinema.mapper.MovieMapper;
import ua.lviv.bas.cinema.repository.GenreRepository;
import ua.lviv.bas.cinema.repository.MovieRepository;
import ua.lviv.bas.cinema.repository.PersonRepository;
import ua.lviv.bas.cinema.scheduler.MovieScheduler;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

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
	private MovieScheduler movieSchedule;
	@InjectMocks
	private MovieService movieService;

	private Movie movie;
	private MovieDetailResponse movieDetailResponse;
	private MovieCardResponse movieCardResponse;
	private MovieCreateRequest createRequest;
	private MovieUpdateRequest updateRequest;
	private Genre genre;
	private Person actor;
	private Person director;
	private Person screenwriter;

	@BeforeEach
	void setUp() {
		genre = new Genre();
		genre.setId(1L);
		genre.setName("Action");

		actor = new Person();
		actor.setId(1L);
		actor.setName("Actor One");
		actor.setRole(PersonRole.ACTOR);

		director = new Person();
		director.setId(2L);
		director.setName("Director One");
		director.setRole(PersonRole.DIRECTOR);

		screenwriter = new Person();
		screenwriter.setId(3L);
		screenwriter.setName("Writer One");
		screenwriter.setRole(PersonRole.SCREENWRITER);

		movie = new Movie();
		movie.setId(1L);
		movie.setTitle("Test Movie");
		movie.setSlug("test-movie");
		movie.setTrailerUrl("https://example.com/trailer");
		movie.setDescription("Test Description");
		movie.setDurationMinutes(120);
		movie.setReleaseDate(LocalDate.now().plusDays(1));
		movie.setEndShowingDate(LocalDate.now().plusDays(30));
		movie.setStatus(MovieStatus.UPCOMING);
		movie.setAgeRating(AgeRating.PEGI_12);
		movie.setPosterFileName("poster.jpg");
		movie.setActors(new HashSet<>(List.of(actor)));
		movie.setDirectors(new HashSet<>(List.of(director)));
		movie.setScreenwriters(new HashSet<>(List.of(screenwriter)));
		movie.setGenres(new HashSet<>(List.of(genre)));

		movieDetailResponse = new MovieDetailResponse();
		movieDetailResponse.setId(1L);
		movieDetailResponse.setTitle("Test Movie");
		movieDetailResponse.setSlug("test-movie");
		movieDetailResponse.setTrailerUrl("https://example.com/trailer");
		movieDetailResponse.setDescription("Test Description");
		movieDetailResponse.setDurationMinutes(120);
		movieDetailResponse.setReleaseDate(LocalDate.now().plusDays(1));
		movieDetailResponse.setEndShowingDate(LocalDate.now().plusDays(30));
		movieDetailResponse.setStatus(MovieStatus.UPCOMING);
		movieDetailResponse.setAgeRating(AgeRating.PEGI_12);
		movieDetailResponse.setPosterFileName("poster.jpg");
		movieDetailResponse.setPosterUrl("/api/movies/1/poster");

		movieCardResponse = new MovieCardResponse();
		movieCardResponse.setId(1L);
		movieCardResponse.setTitle("Test Movie");
		movieCardResponse.setSlug("test-movie");
		movieCardResponse.setDurationMinutes(120);
		movieCardResponse.setAgeRating(AgeRating.PEGI_12);
		movieCardResponse.setReleaseDate(LocalDate.now().plusDays(1));
		movieCardResponse.setStatus(MovieStatus.UPCOMING);
		movieCardResponse.setCurrentlyShowing(false);

		createRequest = new MovieCreateRequest();
		createRequest.setTitle("New Movie");
		createRequest.setTrailerUrl("https://example.com/trailer");
		createRequest.setDescription("Test Description");
		createRequest.setDurationMinutes(120);
		createRequest.setReleaseDate(LocalDate.now().plusDays(1));
		createRequest.setEndShowingDate(LocalDate.now().plusDays(30));
		createRequest.setAgeRating(AgeRating.PEGI_12);
		createRequest.setGenreIds(List.of(1L));
		createRequest.setActorIds(List.of(1L));
		createRequest.setDirectorIds(List.of(2L));
		createRequest.setScreenwriterIds(List.of(3L));

		updateRequest = new MovieUpdateRequest();
		updateRequest.setTitle("Updated Movie");
		updateRequest.setTrailerUrl("https://example.com/updated-trailer");
		updateRequest.setDescription("Updated Description");
		updateRequest.setDurationMinutes(140);
		updateRequest.setReleaseDate(LocalDate.now().plusDays(10));
		updateRequest.setEndShowingDate(LocalDate.now().plusDays(70));
		updateRequest.setAgeRating(AgeRating.PEGI_16);
		updateRequest.setGenreIds(List.of(1L));
		updateRequest.setActorIds(List.of(1L));
		updateRequest.setDirectorIds(List.of(2L));
		updateRequest.setScreenwriterIds(List.of(3L));
		updateRequest.setRemovePoster(false);

		ReflectionTestUtils.setField(movieService, "uploadDir", "test-uploads");
	}

	@Test
	void createMovie_ShouldCreateMovieSuccessfully() {
		when(slugService.generateUniqueSlug("New Movie")).thenReturn("new-movie");
		when(movieRepository.findBySlug("new-movie")).thenReturn(Optional.empty());
		when(movieMapper.toEntity(createRequest)).thenReturn(movie);
		when(movieSchedule.calculateMovieStatus(movie, LocalDate.now())).thenReturn(MovieStatus.UPCOMING);
		when(genreRepository.findAllById(List.of(1L))).thenReturn(List.of(genre));
		when(personRepository.findAllById(List.of(1L))).thenReturn(List.of(actor));
		when(personRepository.findAllById(List.of(2L))).thenReturn(List.of(director));
		when(personRepository.findAllById(List.of(3L))).thenReturn(List.of(screenwriter));
		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toDetailResponse(movie)).thenReturn(movieDetailResponse);

		MovieDetailResponse result = movieService.createMovie(createRequest);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("Test Movie", result.getTitle());
		verify(movieRepository).save(movie);
		verify(movieSchedule).calculateMovieStatus(movie, LocalDate.now());
	}

	@Test
	void createMovie_ShouldThrowException_WhenEndDateBeforeReleaseDate() {
		MovieCreateRequest invalidRequest = new MovieCreateRequest();
		invalidRequest.setTitle("Invalid Movie");
		invalidRequest.setReleaseDate(LocalDate.now().plusDays(10));
		invalidRequest.setEndShowingDate(LocalDate.now().plusDays(5));

		assertThrows(IllegalArgumentException.class, () -> movieService.createMovie(invalidRequest));
	}

	@Test
	void createMovie_ShouldThrowDuplicateEntityException_WhenSlugExists() {
		when(slugService.generateUniqueSlug("New Movie")).thenReturn("existing-movie");
		when(movieRepository.findBySlug("existing-movie")).thenReturn(Optional.of(movie));

		assertThrows(DuplicateEntityException.class, () -> movieService.createMovie(createRequest));
	}

	@Test
	void getMovieById_ShouldReturnMovie() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(movieMapper.toDetailResponse(movie)).thenReturn(movieDetailResponse);

		MovieDetailResponse result = movieService.getMovieById(1L);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("Test Movie", result.getTitle());
	}

	@Test
	void getMovieById_ShouldThrowMovieNotFoundException_WhenMovieNotExists() {
		when(movieRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(MovieNotFoundException.class, () -> movieService.getMovieById(999L));
	}

	@Test
	void getMovieBySlug_ShouldReturnMovie() {
		when(movieRepository.findBySlug("test-movie")).thenReturn(Optional.of(movie));
		when(movieMapper.toDetailResponse(movie)).thenReturn(movieDetailResponse);

		MovieDetailResponse result = movieService.getMovieBySlug("test-movie");

		assertNotNull(result);
		assertEquals("test-movie", result.getSlug());
	}

	@Test
	void getMovieBySlug_ShouldThrowMovieNotFoundException_WhenMovieNotExists() {
		when(movieRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

		assertThrows(MovieNotFoundException.class, () -> movieService.getMovieBySlug("nonexistent"));
	}

	@Test
	void getAllMovies_ShouldReturnAllMovies() {
		when(movieRepository.findAll()).thenReturn(List.of(movie));
		when(movieMapper.toDetailResponse(movie)).thenReturn(movieDetailResponse);

		List<MovieDetailResponse> result = movieService.getAllMovies();

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("Test Movie", result.get(0).getTitle());
	}

	@Test
	void getCurrentlyShowingMovies_ShouldReturnCurrentMovies() {
		LocalDate today = LocalDate.now();
		movie.setReleaseDate(today.minusDays(1));
		movie.setEndShowingDate(today.plusDays(30));

		when(movieRepository.findAll()).thenReturn(List.of(movie));
		when(movieSchedule.calculateMovieStatus(movie, today)).thenReturn(MovieStatus.CURRENT);

		MovieCardResponse cardResponse = new MovieCardResponse();
		cardResponse.setId(1L);
		cardResponse.setTitle("Test Movie");
		when(movieMapper.toCardResponse(movie)).thenReturn(cardResponse);

		List<MovieCardResponse> result = movieService.getCurrentlyShowingMovies();

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(movieSchedule).calculateMovieStatus(movie, today);
	}

	@Test
	void getUpcomingMovies_ShouldReturnUpcomingMovies() {
		LocalDate today = LocalDate.now();
		movie.setReleaseDate(today.plusDays(1));
		movie.setEndShowingDate(today.plusDays(30));

		when(movieRepository.findAll()).thenReturn(List.of(movie));
		when(movieSchedule.calculateMovieStatus(movie, today)).thenReturn(MovieStatus.UPCOMING);

		MovieCardResponse cardResponse = new MovieCardResponse();
		cardResponse.setId(1L);
		cardResponse.setTitle("Test Movie");
		when(movieMapper.toCardResponse(movie)).thenReturn(cardResponse);

		List<MovieCardResponse> result = movieService.getUpcomingMovies();

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(movieSchedule).calculateMovieStatus(movie, today);
	}

	@Test
	void getArchivedMovies_ShouldReturnArchivedMovies() {
		LocalDate today = LocalDate.now();
		movie.setReleaseDate(today.minusDays(10));
		movie.setEndShowingDate(today.minusDays(1));

		when(movieRepository.findAll()).thenReturn(List.of(movie));
		when(movieSchedule.calculateMovieStatus(movie, today)).thenReturn(MovieStatus.ARCHIVED);

		MovieCardResponse cardResponse = new MovieCardResponse();
		cardResponse.setId(1L);
		cardResponse.setTitle("Test Movie");
		when(movieMapper.toCardResponse(movie)).thenReturn(cardResponse);

		List<MovieCardResponse> result = movieService.getArchivedMovies();

		assertNotNull(result);
		assertEquals(1, result.size());
		verify(movieSchedule).calculateMovieStatus(movie, today);
	}

	@Test
	void deleteMovie_ShouldDeleteMovieSuccessfully() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

		movieService.deleteMovie(1L);

		verify(movieRepository).delete(movie);
	}

	@Test
	void deleteMovie_ShouldThrowMovieNotFoundException_WhenMovieNotExists() {
		when(movieRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(MovieNotFoundException.class, () -> movieService.deleteMovie(999L));
	}

	@Test
	void updateMovie_ShouldUpdateMovieSuccessfully() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(slugService.generateUniqueSlug("Updated Movie")).thenReturn("updated-movie");
		when(slugService.isSlugAvailableForMovie("updated-movie", 1L)).thenReturn(true);
		when(movieSchedule.calculateMovieStatus(movie, LocalDate.now())).thenReturn(MovieStatus.UPCOMING);
		when(genreRepository.findAllById(List.of(1L))).thenReturn(List.of(genre));
		when(personRepository.findAllById(List.of(1L))).thenReturn(List.of(actor));
		when(personRepository.findAllById(List.of(2L))).thenReturn(List.of(director));
		when(personRepository.findAllById(List.of(3L))).thenReturn(List.of(screenwriter));
		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toDetailResponse(movie)).thenReturn(movieDetailResponse);

		MovieDetailResponse result = movieService.updateMovie(1L, updateRequest);

		assertNotNull(result);
		verify(movieRepository).save(movie);
		verify(movieSchedule).calculateMovieStatus(movie, LocalDate.now());
	}

	@Test
	void updateMovie_ShouldThrowException_WhenEndDateBeforeReleaseDate() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

		MovieUpdateRequest invalidRequest = new MovieUpdateRequest();
		invalidRequest.setTitle("Updated Movie");
		invalidRequest.setReleaseDate(LocalDate.now().plusDays(10));
		invalidRequest.setEndShowingDate(LocalDate.now().plusDays(5));

		assertThrows(IllegalArgumentException.class, () -> movieService.updateMovie(1L, invalidRequest));
	}

	@Test
	void updateMovie_ShouldThrowDuplicateEntityException_WhenNewSlugExists() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(slugService.generateUniqueSlug("Updated Movie")).thenReturn("existing-slug");
		when(slugService.isSlugAvailableForMovie("existing-slug", 1L)).thenReturn(false);

		assertThrows(DuplicateEntityException.class, () -> movieService.updateMovie(1L, updateRequest));
	}

	@Test
	void getMovieEntityById_ShouldReturnMovieEntity() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

		Movie result = movieService.getMovieEntityById(1L);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("Test Movie", result.getTitle());
	}

	@Test
	void getMovieEntityById_ShouldThrowMovieNotFoundException_WhenMovieNotExists() {
		when(movieRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(MovieNotFoundException.class, () -> movieService.getMovieEntityById(999L));
	}

	@Test
	void searchMoviesForSessionCreation_ShouldReturnAvailableMovies() {
		LocalDate sessionDate = LocalDate.now().plusDays(5);
		when(movieRepository.findByTitleContainingIgnoreCase(eq("test"), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(movie)));

		List<MovieSessionSearchResponse> result = movieService.searchMoviesForSessionCreation("test", sessionDate);

		assertNotNull(result);
	}

	@Test
	void searchMoviesForSessionCreation_ShouldReturnAllMovies_WhenSearchTermEmpty() {
		LocalDate sessionDate = LocalDate.now().plusDays(5);
		when(movieRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(movie)));

		List<MovieSessionSearchResponse> result = movieService.searchMoviesForSessionCreation("", sessionDate);

		assertNotNull(result);
	}

	@Test
	void searchMovies_ShouldReturnPaginatedResults() {
		Page<Movie> moviePage = new PageImpl<>(List.of(movie));
		when(movieRepository.findByTitleContainingIgnoreCase(eq("test"), any(Pageable.class))).thenReturn(moviePage);

		MovieCardResponse cardResponse = new MovieCardResponse();
		cardResponse.setId(1L);
		cardResponse.setTitle("Test Movie");
		when(movieMapper.toCardResponse(movie)).thenReturn(cardResponse);

		Page<MovieCardResponse> result = movieService.searchMovies("test", Pageable.unpaged());

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
	}

	@Test
	void searchMovies_ShouldReturnAllMovies_WhenSearchTermNull() {
		Page<Movie> moviePage = new PageImpl<>(List.of(movie));
		when(movieRepository.findAll(any(Pageable.class))).thenReturn(moviePage);

		MovieCardResponse cardResponse = new MovieCardResponse();
		cardResponse.setId(1L);
		cardResponse.setTitle("Test Movie");
		when(movieMapper.toCardResponse(movie)).thenReturn(cardResponse);

		Page<MovieCardResponse> result = movieService.searchMovies(null, Pageable.unpaged());

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
	}

	@Test
	void getMoviesPaginated_ShouldReturnPaginatedMovies() {
		Page<Movie> moviePage = new PageImpl<>(List.of(movie));
		when(movieRepository.findAll(any(Pageable.class))).thenReturn(moviePage);
		when(movieMapper.toDetailResponse(movie)).thenReturn(movieDetailResponse);

		Page<MovieDetailResponse> result = movieService.getMoviesPaginated(Pageable.unpaged());

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
	}
}