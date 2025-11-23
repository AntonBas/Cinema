package ua.lviv.bas.cinema.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.mapper.MovieMapper;
import ua.lviv.bas.cinema.repository.GenreRepository;
import ua.lviv.bas.cinema.repository.MovieRepository;
import ua.lviv.bas.cinema.repository.PersonRepository;

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
		genre = Genre.builder().id(1L).name("Action").build();
		actor = Person.builder().id(1L).name("Actor One").role(PersonRole.ACTOR).build();
		director = Person.builder().id(2L).name("Director One").role(PersonRole.DIRECTOR).build();
		screenwriter = Person.builder().id(3L).name("Writer One").role(PersonRole.SCREENWRITER).build();

		movie = Movie.builder().id(1L).title("Test Movie").slug("test-movie").trailerUrl("https://example.com/trailer")
				.description("Test Description").durationMinutes(120).releaseDate(LocalDate.now().plusDays(1))
				.endShowingDate(LocalDate.now().plusDays(30)).status(MovieStatus.UPCOMING).ageRating(AgeRating.PEGI_12)
				.posterFileName("poster.jpg").actors(new HashSet<>(List.of(actor)))
				.directors(new HashSet<>(List.of(director))).screenwriters(new HashSet<>(List.of(screenwriter)))
				.genres(new HashSet<>(List.of(genre))).build();

		movieDetailResponse = MovieDetailResponse.builder().id(1L).title("Test Movie").slug("test-movie")
				.trailerUrl("https://example.com/trailer").description("Test Description").durationMinutes(120)
				.releaseDate(LocalDate.now().plusDays(1)).endShowingDate(LocalDate.now().plusDays(30))
				.status(MovieStatus.UPCOMING).ageRating(AgeRating.PEGI_12).posterFileName("poster.jpg")
				.actorIds(List.of(1L)).directorIds(List.of(2L)).screenwriterIds(List.of(3L)).genreIds(List.of(1L))
				.build();

		movieCardResponse = MovieCardResponse.builder().id(1L).title("Test Movie").slug("test-movie")
				.durationMinutes(120).ageRating(AgeRating.PEGI_12).releaseDate(LocalDate.now().plusDays(1))
				.status(MovieStatus.UPCOMING).currentlyShowing(false).build();

		createRequest = MovieCreateRequest.builder().title("New Movie").trailerUrl("https://example.com/trailer")
				.description("Test Description").durationMinutes(120).releaseDate(LocalDate.now().plusDays(1))
				.endShowingDate(LocalDate.now().plusDays(30)).ageRating(AgeRating.PEGI_12).genreIds(List.of(1L))
				.actorIds(List.of(1L)).directorIds(List.of(2L)).screenwriterIds(List.of(3L)).build();

		updateRequest = MovieUpdateRequest.builder().title("Updated Movie")
				.trailerUrl("https://example.com/updated-trailer").description("Updated Description")
				.durationMinutes(140).releaseDate(LocalDate.now().plusDays(10))
				.endShowingDate(LocalDate.now().plusDays(70)).ageRating(AgeRating.PEGI_16).genreIds(List.of(1L))
				.actorIds(List.of(1L)).directorIds(List.of(2L)).screenwriterIds(List.of(3L)).removePoster(false)
				.build();

		org.springframework.test.util.ReflectionTestUtils.setField(movieService, "uploadDir", "test-uploads");
	}

	@Test
	void createMovie_ShouldCreateMovieSuccessfully() {
		when(slugService.generateUniqueSlug("New Movie")).thenReturn("new-movie");
		when(movieRepository.findBySlug("new-movie")).thenReturn(Optional.empty());
		when(movieMapper.toEntity(createRequest)).thenReturn(movie);
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
	void getMovieBySlug_ShouldReturnMovie() {
		when(movieRepository.findBySlug("test-movie")).thenReturn(Optional.of(movie));
		when(movieMapper.toDetailResponse(movie)).thenReturn(movieDetailResponse);

		MovieDetailResponse result = movieService.getMovieBySlug("test-movie");

		assertNotNull(result);
		assertEquals("test-movie", result.getSlug());
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
		movie.setReleaseDate(LocalDate.now().minusDays(1));
		movie.setEndShowingDate(LocalDate.now().plusDays(30));

		when(movieRepository.findAll()).thenReturn(List.of(movie));
		when(movieMapper.toCardResponse(movie)).thenReturn(movieCardResponse);

		List<MovieCardResponse> result = movieService.getCurrentlyShowingMovies();

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void getUpcomingMovies_ShouldReturnUpcomingMovies() {
		movie.setReleaseDate(LocalDate.now().plusDays(1));
		movie.setEndShowingDate(LocalDate.now().plusDays(30));

		when(movieRepository.findAll()).thenReturn(List.of(movie));
		when(movieMapper.toCardResponse(movie)).thenReturn(movieCardResponse);

		List<MovieCardResponse> result = movieService.getUpcomingMovies();

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void getArchivedMovies_ShouldReturnArchivedMovies() {
		movie.setReleaseDate(LocalDate.now().minusDays(10));
		movie.setEndShowingDate(LocalDate.now().minusDays(1));

		when(movieRepository.findAll()).thenReturn(List.of(movie));
		when(movieMapper.toCardResponse(movie)).thenReturn(movieCardResponse);

		List<MovieCardResponse> result = movieService.getArchivedMovies();

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void deleteMovie_ShouldDeleteMovieSuccessfully() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

		movieService.deleteMovie(1L);

		verify(movieRepository).delete(movie);
	}

	@Test
	void updateMovie_ShouldUpdateMovieSuccessfully() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(slugService.generateUniqueSlug("Updated Movie")).thenReturn("updated-movie");
		when(slugService.isSlugAvailableForMovie("updated-movie", 1L)).thenReturn(true);
		when(genreRepository.findAllById(List.of(1L))).thenReturn(List.of(genre));
		when(personRepository.findAllById(List.of(1L))).thenReturn(List.of(actor));
		when(personRepository.findAllById(List.of(2L))).thenReturn(List.of(director));
		when(personRepository.findAllById(List.of(3L))).thenReturn(List.of(screenwriter));
		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toDetailResponse(movie)).thenReturn(movieDetailResponse);

		MovieDetailResponse result = movieService.updateMovie(1L, updateRequest);

		assertNotNull(result);
		verify(movieRepository).save(movie);
	}

	@Test
	void getMovieEntityById_ShouldReturnMovieEntity() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

		Movie result = movieService.getMovieEntityById(1L);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("Test Movie", result.getTitle());
	}
}